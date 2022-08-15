package com.vaadin.addon.pagination;

import java.util.stream.Stream;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderWrapper;
import com.vaadin.data.provider.Query;

/**
 *
 */
public class PaginationDataProviderWrapper<T, F> extends DataProviderWrapper<T, F, F> {

    private PaginationResource pagination;

    public PaginationDataProviderWrapper(DataProvider<T, F> dataProvider) {
        super(dataProvider);
    }

    @Override
    protected F getFilter(Query<T, F> query) {
        return query.getFilter().orElse(null);
    }

    @Override
    public Stream<T> fetch(Query<T, F> t) {
        return dataProvider.fetch(toPaginatedQuery(t));
    }

    @Override
    public int size(Query<T, F> t) {
        Query<T, F> paginatedQuery = toPaginatedQuery(t);
        int size = dataProvider.size(paginatedQuery);
        return Integer.min(size, paginatedQuery.getLimit()); //com.vaadin.data.provider.ListDataProvider.size ignores the limit
    }

    public void setPaginationResource(PaginationResource pagination) {
        this.pagination = pagination;
        refreshAll();
    }

    private Query<T, F> toPaginatedQuery(Query<T, F> t) {
        if (pagination == null) {
            return t;
        } else {
            int offset = pagination.offset() + t.getOffset();
            int limit = Integer.min(t.getLimit(), pagination.toIndex() - pagination.fromIndex());
            return new Query<>(offset, limit, t.getSortOrders(), t.getInMemorySorting(), getFilter(t));
        }
    }
}