package de.unistuttgart.iste.gits.common.util;

import de.unistuttgart.iste.gits.generated.dto.Pagination;
import de.unistuttgart.iste.gits.generated.dto.PaginationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PaginationUtilTest {

    /**
     * Given no pagination dto
     * When createPageable is called
     * Then an unpaged pageable is returned
     */
    @Test
    public void testCreatePageableNoPagination() {
        Pageable pageable = PaginationUtil.createPageable(null, null);

        assertThat(pageable.isPaged(), equalTo(false));

        pageable = PaginationUtil.createPageable(null, Sort.by("field"));

        assertThat(pageable.isPaged(), equalTo(false));
    }

    /**
     * Given a pagination dto
     * When createPageable is called
     * Then a pageable matching the pagination dto is returned
     */
    @Test
    public void testCreatePageable() {
        Pagination pagination = Pagination.builder()
                .setPage(1)
                .setSize(10)
                .build();

        Pageable pageable = PaginationUtil.createPageable(pagination, Sort.unsorted());
        assertThat(pageable.isPaged(), equalTo(true));
        assertThat(pageable.getPageNumber(), equalTo(1));
        assertThat(pageable.getPageSize(), equalTo(10));

        assertThat(pageable.getSort(), equalTo(Sort.unsorted()));
    }

    /**
     * Given a number of total elements
     * When unpagedPaginationInfo is called
     * Then the correct pagination info dto is returned
     */
    @Test
    public void testCreateUnpagedPaginationInfo() {
        PaginationInfo Pagination = PaginationUtil.unpagedPaginationInfo(10);

        assertThat(Pagination.getPage(), equalTo(0));
        assertThat(Pagination.getSize(), equalTo(10));
        assertThat(Pagination.getTotalElements(), equalTo(10));
        assertThat(Pagination.getTotalPages(), equalTo(1));
        assertThat(Pagination.getHasNext(), equalTo(false));
    }

    /**
     * Given a page
     * When createPaginationInfo is called
     * Then the correct pagination info dto is returned
     */
    @Test
    public void testCreatePaginationInfo() {
        Page<?> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 20);
        PaginationInfo Pagination = PaginationUtil.createPaginationInfo(page);

        assertThat(Pagination.getPage(), equalTo(0));
        assertThat(Pagination.getSize(), equalTo(10));
        assertThat(Pagination.getTotalElements(), equalTo(20));
        assertThat(Pagination.getTotalPages(), equalTo(2));
        assertThat(Pagination.getHasNext(), equalTo(true));
    }
}
