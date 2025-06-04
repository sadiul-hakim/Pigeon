package xyz.sadiulhakim.util;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {
    private PageUtil() {
    }

    public static PaginationResult prepareResult(Page<?> page) {
        return prepareResult(page, page.getContent());
    }

    public static PaginationResult prepareResult(Page<?> page, List<?> data) {

        var result = new PaginationResult();
        result.setFirst(page.isFirst());
        result.setLast(page.isLast());
        result.setHasNext(page.hasNext());
        result.setHasPrev(page.hasPrevious());
        result.setTotalRecords(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setCurrentPage(page.getNumber());
        result.setPageSize(page.getSize());

        int start = result.getCurrentPage() * result.getPageSize() + 1;
        long end = Math.min(start + page.getNumberOfElements() - 1, result.getTotalRecords());

        result.setStart(start);
        result.setEnd(end);
        result.setData(new ArrayList<>(data));
        return result;
    }
}