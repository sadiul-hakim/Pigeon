package xyz.sadiulhakim.util;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResult {
    private List<Object> data = new ArrayList<>();
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrev;
    private long totalRecords;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private int start;
    private long end;
}
