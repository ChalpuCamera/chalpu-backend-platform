package com.example.chalpuplatform.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

@Slf4j
public class PagedIterator<T> implements Iterator<T> {

    private static final int PAGE_SIZE = 500;

    private final Function<Pageable, Page<T>> pageLoader;
    private Iterator<T> currentIterator = Collections.emptyIterator();
    private int currentPage = 0;
    private boolean hasMorePages = true;

    public PagedIterator(Function<Pageable, Page<T>> pageLoader) {
        this.pageLoader = pageLoader;
        loadNextPage();
    }

    private void loadNextPage() {
        if (!hasMorePages) {
            return;
        }

        log.debug("페이지 로딩 시작: page={}", currentPage);
        Page<T> page = pageLoader.apply(PageRequest.of(currentPage++, PAGE_SIZE));
        log.debug("페이지 로드 완료: totalPages={}, currentElements={}",
            page.getTotalPages(), page.getNumberOfElements());

        currentIterator = page.getContent().iterator();
        hasMorePages = page.hasNext();
    }

    @Override
    public boolean hasNext() {
        if (currentIterator.hasNext()) {
            return true;
        }

        if (hasMorePages) {
            loadNextPage();
            return currentIterator.hasNext();
        }

        return false;
    }

    @Override
    public T next() {
        return currentIterator.next();
    }
}
