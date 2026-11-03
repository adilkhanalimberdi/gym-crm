package com.epam.gymcrm.dto;

import com.epam.gymcrm.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageQueryTest {

    @Test
    void firstPage_startsAtTheBeginning() {
        assertEquals(0, PageQuery.firstPage().offset());
        assertEquals(20, PageQuery.firstPage().size());
    }

    @Test
    void offset_skipsThePreviousPages() {
        assertEquals(30, new PageQuery(3, 10).offset());
    }

    @Test
    void rejectsNegativePage() {
        assertThrows(InvalidRequestException.class, () -> new PageQuery(-1, 10));
    }

    @Test
    void rejectsSizeOutsideTheAllowedRange() {
        assertThrows(InvalidRequestException.class, () -> new PageQuery(0, 0));
        assertThrows(InvalidRequestException.class, () -> new PageQuery(0, 101));
    }
}
