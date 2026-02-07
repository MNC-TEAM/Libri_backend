package monochrome.libri.note.service;

public interface NoteReactionService {
    void like(long noteId, long memberId);
    void unlike(long noteId, long memberId);
    void bookmark(long noteId, long memberId);
    void unbookmark(long noteId, long memberId);
}
