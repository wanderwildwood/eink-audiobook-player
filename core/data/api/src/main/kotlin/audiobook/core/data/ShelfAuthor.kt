package audiobook.core.data

/**
 * What the Author shelf files this book under.
 *
 * Only Author-Book Mode gives a book a [BookContent.folderName]; the other two folder modes leave
 * it null for every book, which would collapse the whole library into one unnamed shelf. The
 * author tag is what the shelf is named after anyway, so it stands in when there is no folder.
 */
public val BookContent.shelfAuthor: String? get() = folderName ?: author
