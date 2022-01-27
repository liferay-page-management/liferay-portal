import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.asset.list.service.AssetListEntryLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import java.util.Map;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;

	public class CollectionDisplayTest {
		public static void main(String[] args) {

		// Add a test site

		ServiceContext serviceContext = new ServiceContext();

		long defaultCompanyId = PortalUtil.getDefaultCompanyId();

		Company defaultCompany = CompanyLocalServiceUtil.getCompany(defaultCompanyId);

		serviceContext.setCompanyId(defaultCompanyId);

		User defaultUser = UserLocalServiceUtil.fetchUserByScreenName(defaultCompanyId, "test");

		long defaultUserId = defaultUser.getUserId();

		Group group = GroupLocalServiceUtil.addGroup(
				defaultUserId,
				GroupConstants.DEFAULT_PARENT_GROUP_ID, null, 0, 0,
				HashMapBuilder.put(
					LocaleUtil.getDefault(), "Test Site Name"
				).build(),
				(Map<Locale, String>)null, GroupConstants.TYPE_SITE_OPEN, true,
				GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, null, true, true,
				serviceContext);

		long groupId = group.getGroupId();

		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(defaultUserId);

		String pageName = "Test Page Name";
		String pageFriendlyUrl = "/test-page-name";
		String vocabularyName = "Vocabulary Name";
		String categoryName = "Category Name";
		String webContentTitle = "Web Content Title";
		String webContentDescription = "Web Content Description";
		String webContentContent = "<?xml version=\"1.0\"?>\n\n<root available-locales=\"en_US\" default-locale=\"en_US\" version=\"1.0\">\n\t<dynamic-element index-type=\"text\" instance-id=\"NSvQsR2d\" name=\"content\" type=\"rich_text\">\n\t\t<dynamic-content language-id=\"en_US\"><![CDATA[<p>Content</p>]]></dynamic-content>\n\t</dynamic-element>\n</root>";

		// Add three vocabularies

		for (int i = 1; i <= 3; i++) {
			AssetVocabularyLocalServiceUtil.addVocabulary(
					defaultUserId, groupId,
					vocabularyName + i, serviceContext);
		}

		// Add three categories to each vocabulary

		AssetVocabulary vocabulary1 = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, vocabularyName + "1");

		AssetCategory categoryA1 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "A1",
				vocabulary1.getVocabularyId(), serviceContext);

		AssetCategory categoryA2 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "A2",
				vocabulary1.getVocabularyId(), serviceContext);

		AssetCategory categoryA3 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "A3",
				vocabulary1.getVocabularyId(), serviceContext);

		AssetVocabulary vocabulary2 = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, vocabularyName + "2");

		AssetCategory categoryB1 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "B1",
				vocabulary2.getVocabularyId(), serviceContext);

		AssetCategory categoryB2 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "B2",
				vocabulary2.getVocabularyId(), serviceContext);

		AssetCategory categoryB3 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "B3",
				vocabulary2.getVocabularyId(), serviceContext);

		AssetVocabulary vocabulary3 = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, vocabularyName + "3");

		AssetCategory categoryC1 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "C1",
				vocabulary3.getVocabularyId(), serviceContext);

		AssetCategory categoryC2 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "C2",
				vocabulary3.getVocabularyId(), serviceContext);

		AssetCategory categoryC3 = AssetCategoryLocalServiceUtil.addCategory(
				defaultUserId, groupId, categoryName + "C3",
				vocabulary3.getVocabularyId(), serviceContext);

		// Add 100 web contents with categories

		long[] categoryIds1 = [
				categoryA1.getCategoryId(),
				categoryA2.getCategoryId(),
				categoryA3.getCategoryId(),
				categoryB1.getCategoryId(),
				categoryB2.getCategoryId(),
				categoryB3.getCategoryId(),
				categoryC1.getCategoryId(),
				categoryC2.getCategoryId(),
				categoryC3.getCategoryId()];

		ServiceContext serviceContext1 = new ServiceContext();

		serviceContext1.setCompanyId(defaultCompanyId);
		serviceContext1.setScopeGroupId(groupId);
		serviceContext1.setUserId(defaultUserId);

		serviceContext1.setAssetCategoryIds(categoryIds1);

		for (int i = 1; i <= 25; i++) {
			Map<Locale, String> titleMap = HashMapBuilder.put(
					LocaleUtil.getDefault(), webContentTitle + i
			).build();

			Map<Locale, String> descriptionMap = HashMapBuilder.put(
					LocaleUtil.getDefault(), webContentDescription + i
			).build();

			JournalArticleLocalServiceUtil.addArticle(
					null, defaultUserId, groupId,
					JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID, titleMap,
					descriptionMap, webContentContent, "BASIC-WEB-CONTENT",
					"BASIC-WEB-CONTENT", serviceContext1);
		}

		long[] categoryIds2 = [
				categoryA1.getCategoryId(),
				categoryB1.getCategoryId(),
				categoryC1.getCategoryId()];

		ServiceContext serviceContext2 = new ServiceContext();

		serviceContext2.setCompanyId(defaultCompanyId);
		serviceContext2.setScopeGroupId(groupId);
		serviceContext2.setUserId(defaultUserId);

		serviceContext2.setAssetCategoryIds(categoryIds2);

		for (int i = 26; i <= 50; i++) {
			JournalArticleLocalServiceUtil.addArticle(
					null, defaultUserId, groupId,
					JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					HashMapBuilder.put(
						LocaleUtil.getDefault(), webContentTitle + i
					).build(),
					HashMapBuilder.put(
							LocaleUtil.getDefault(), webContentDescription + i
					).build(), webContentContent, "BASIC-WEB-CONTENT",
					"BASIC-WEB-CONTENT", serviceContext2);
		}

		long[] categoryIds3 = [
				categoryA1.getCategoryId(),
				categoryB2.getCategoryId(),
				categoryC2.getCategoryId()];

		ServiceContext serviceContext3 = new ServiceContext();

		serviceContext3.setCompanyId(defaultCompanyId);
		serviceContext3.setScopeGroupId(groupId);
		serviceContext3.setUserId(defaultUserId);

		serviceContext3.setAssetCategoryIds(categoryIds3);

		for (int i = 51; i <= 75; i++) {
			JournalArticleLocalServiceUtil.addArticle(
					null, defaultUserId, groupId,
					JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					HashMapBuilder.put(
						LocaleUtil.getDefault(), webContentTitle + i
					).build(),
					HashMapBuilder.put(
							LocaleUtil.getDefault(), webContentDescription + i
					).build(), webContentContent, "BASIC-WEB-CONTENT",
					"BASIC-WEB-CONTENT", serviceContext3);
		}

		long[] categoryIds4 = [categoryA1.getCategoryId()];

		ServiceContext serviceContext4 = new ServiceContext();

		serviceContext4.setCompanyId(defaultCompanyId);
		serviceContext4.setScopeGroupId(groupId);
		serviceContext4.setUserId(defaultUserId);

		serviceContext4.setAssetCategoryIds(categoryIds4);

		for (int i = 76; i <= 100; i++) {
			JournalArticleLocalServiceUtil.addArticle(
					null, defaultUserId, groupId,
					JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					HashMapBuilder.put(
						LocaleUtil.getDefault(), webContentTitle + i
					).build(),
					HashMapBuilder.put(
							LocaleUtil.getDefault(), webContentDescription + i
					).build(), webContentContent, "BASIC-WEB-CONTENT",
					"BASIC-WEB-CONTENT", serviceContext4);
		}

		// Add a dynamic collection for Web Content Article and Basic Web Content

		DDMStructure ddmStructure = DDMStructureLocalServiceUtil.getStructure(
				defaultCompany.getGroupId(), PortalUtil.getClassNameId(JournalArticle.class), "BASIC-WEB-CONTENT");

		AssetListEntry assetListEntry =
				AssetListEntryLocalServiceUtil.addDynamicAssetListEntry(
						defaultUserId, groupId,
						"Collection Title", UnicodePropertiesBuilder.create(
						true
				).put(
						"anyAssetType",
						String.valueOf(PortalUtil.getClassNameId(JournalArticle.class))
				).put(
						"anyClassTypeJournalArticleAssetRendererFactory",
						String.valueOf(ddmStructure.getStructureId())
				).put(
						"classNameIds", String.valueOf(PortalUtil.getClassNameId(JournalArticle.class))
				).put(
						"classTypeIdsJournalArticleAssetRendererFactory", String.valueOf(ddmStructure.getStructureId())
				).put(
						"groupIds", String.valueOf(groupId)
				).put(
						"orderByColumn1", "modifiedDate"
				).put(
						"orderByColumn2", "title"
				).put(
						"orderByType1", "DESC"
				).put(
						"orderByType2", "ASC"
				).put(
						"queryAndOperator0", "true"
				).put(
						"queryContains0", "true"
				).put(
						"queryName0", "assetTags"
				).put(
						"subtypeFieldsFilterEnabledJournalArticleAssetRendererFactory", "false"
				).buildString(), serviceContext);

		// Add a content page

		Layout layout = LayoutLocalServiceUtil.addLayout(
				defaultUserId, groupId, false, LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, pageName, StringPool.BLANK, StringPool.BLANK,
				LayoutConstants.TYPE_CONTENT, false, pageFriendlyUrl,
				serviceContext);

		// Add a
	}
}