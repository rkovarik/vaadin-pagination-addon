package com.vaadin.addon.pagination.demo;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.pagination.PaginationDataProviderWrapper;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.ui.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.addon.pagination.Pagination;
import com.vaadin.addon.pagination.PaginationChangeListener;
import com.vaadin.addon.pagination.PaginationResource;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;

@SpringUI
@Theme("demo")
@Title("Pagination Add-on Demo")
@Widgetset("com.vaadin.addon.pagination.Widgetset")
@SuppressWarnings("serial")
public class DemoUI extends UI {

    private static final Logger log = LoggerFactory.getLogger(DemoUI.class);

    public static List<User> userList = new ArrayList<>();

    @Autowired
    UserRepository userRepository;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(nativeSublist(), "native_sublist");
        tabSheet.addTab(jpaPagable(), "jpa_pagable");
        tabSheet.addTab(gridFindAll(), "grid_findAll");
        setContent(tabSheet);
    }

    public VerticalLayout nativeSublist() {

        final int page = 1;
        final int limit = 20;

        final List<User> users = userList;

        final Grid<User> grid = createGrid(users);
        final Pagination pagination = createPagination(page, limit, grid);
        pagination.addPageChangeListener(new PaginationChangeListener() {
            @Override
            public void changed(PaginationResource event) {
                log.debug("nativeSublist: {}", event.toString());
                refreshDataProvider(event, grid);
                grid.scrollToStart();
            }
        });
        final VerticalLayout layout = createContent(grid, pagination);
        return layout;
    }

    public VerticalLayout jpaPagable() {

        final int page = 1;
        final int limit = 10;

        final Page<User> users = findAll(0, limit);
        final long total = users.getTotalElements();

        final Grid grid = createGrid(users.getContent());
        final Pagination pagination = createPagination(page, limit, grid);
        pagination.addPageChangeListener(new PaginationChangeListener() {
            @Override
            public void changed(PaginationResource event) {
                log.debug("jpaPagable : {}", event.toString());
                Page<User> users = findAll(event.pageIndex(), event.limit());
                pagination.setTotalCount(users.getTotalElements());
                grid.scrollToStart();
            }
        });

        final VerticalLayout layout = createContent(grid, pagination);
        return layout;
    }

    public Component gridFindAll() {

        final int page = 1;
        final int limit = 50;

        final List<User> users = userRepository.findAll();
        final long total = users.size();

        final Grid grid = createGrid(userList);
        final Pagination pagination = createPagination(page, limit, grid);
        pagination.setItemsPerPageVisible(false);
        pagination.addPageChangeListener(new PaginationChangeListener() {
            @Override
            public void changed(PaginationResource event) {
                log.debug("gridFindAll : {}", event.toString());
                pagination.setTotalCount(users.size());
                refreshDataProvider(event, grid);
                grid.scrollToStart();
            }
        });
        final VerticalLayout layout = createContent(grid, pagination);
        return layout;
    }

    private void refreshDataProvider(PaginationResource event, Grid<User> grid) {
        ((PaginationDataProviderWrapper<?, ?>) grid.getDataProvider()).setPaginationResource(event);
    }

    public Page<User> findAll(int page, int size) {
        Pageable pageable = new PageRequest(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users;
    }

    private Grid createGrid(List<User> users) {
        final Grid<User> grid = new Grid<>();
        DataProvider<User, ?> dataProvider = new ListDataProvider<>(users);
        grid.setDataProvider(new PaginationDataProviderWrapper<>(dataProvider));
        grid.setSizeFull();
        grid.addColumn(User::getId).setCaption("ID");
        grid.addColumn(User::getName).setCaption("Name");
        grid.addColumn(User::getEmail).setCaption("Email");
        return grid;
    }

    private Pagination createPagination(int page, int limit, Grid<User> grid) {
        PaginationResource paginationResource = PaginationResource.newBuilder()
                .setTotal(grid.getDataProvider().size(new Query<>()))
                .setPage(page)
                .setLimit(limit)
                .build();
        final Pagination pagination = new Pagination(paginationResource);
        pagination.setItemsPerPage(10, 20, 50, 100);
        refreshDataProvider(paginationResource, grid);
        return pagination;
    }

    private VerticalLayout createContent(Grid grid, Pagination pagination) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.addComponents(grid, pagination);
        layout.setExpandRatio(grid, 1f);
        return layout;
    }
}
