package co.edu.uco.ucokudos.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentTest {

    @Test
    void twoAttachmentsWithTheSameBytesAreEqualEvenIfTheArraysAreDifferentObjects() {
        final Attachment first = new Attachment("a.pdf", "application/pdf", new byte[]{1, 2, 3});
        final Attachment second = new Attachment("a.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void aDifferentByteOrNameOrTypeMakesThemDifferent() {
        final Attachment base = new Attachment("a.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertNotEquals(base, new Attachment("a.pdf", "application/pdf", new byte[]{1, 2, 4}));
        assertNotEquals(base, new Attachment("b.pdf", "application/pdf", new byte[]{1, 2, 3}));
        assertNotEquals(base, new Attachment("a.pdf", "image/png", new byte[]{1, 2, 3}));
        assertNotEquals(base, null);
    }

    @Test
    void thePrintedFormShowsTheSizeAndNeverTheContent() {
        final String text = new Attachment("a.pdf", "application/pdf", new byte[]{7, 8, 9, 10}).toString();

        assertTrue(text.contains("a.pdf") && text.contains("size=4"), text);
        assertFalse(text.contains("[B@") || text.contains("7, 8"), text);
    }
}
