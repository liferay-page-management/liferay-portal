create index IX_D648668A on StyleBookEntry (groupId, ctCollectionId);
create index IX_20478749 on StyleBookEntry (groupId, name[$COLUMN_LENGTH:75$], ctCollectionId);
create unique index IX_38C3C389 on StyleBookEntry (groupId, styleBookEntryKey[$COLUMN_LENGTH:75$], ctCollectionId);