import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.layout.util.structure.CollectionStyledLayoutStructureItem;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.layout.util.LayoutCopyHelper;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.layout.util.structure.ColumnLayoutStructureItem;
import com.liferay.fragment.service.FragmentEntryLinkLocalServiceUtil;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.DefaultFragmentRendererContext;
import com.liferay.fragment.renderer.FragmentRenderer ;
import com.liferay.fragment.renderer.FragmentRendererTracker;
import com.liferay.portal.scripting.groovy.internal.GroovyExecutor;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.layout.util.structure.LayoutStructure;
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

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.framework.BundleContext;

class CollectionDisplayTest {
	static String _KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR =
			"com.liferay.fragment.entry.processor.freemarker." +
					"FreeMarkerFragmentEntryProcessor";
	static String pageName = "Test Page Name";
	static String pageFriendlyUrl = "/test-page-name";
	static String vocabularyName = "Vocabulary Name";
	static String categoryName = "Category Name";
	static String webContentTitle = "Web Content Title";
	static String webContentDescription = "Web Content Description";
	static String webContentContent = "<?xml version=\"1.0\"?>\n\n<root available-locales=" +
			"\"en_US\" default-locale=\"en_US\" version=\"1.0\">\n\t<dynamic-element index-type=" +
			"\"text\" instance-id=\"NSvQsR2d\" name=\"content\" type=" +
			"\"rich_text\">\n\t\t<dynamic-content language-id=" +
			"\"en_US\"><![CDATA[<p>Content</p>]]></dynamic-content>\n\t</dynamic-element>\n</root>";
	static String fragmentEntryKey = "com.liferay.fragment.renderer.collection.filter." +
			"internal.CollectionFilterFragmentRenderer";

	static long getDefaultUserId(long companyId) {
		User defaultUser = UserLocalServiceUtil.fetchUserByScreenName(companyId, "test");

		return defaultUser.getUserId();
	}

	static long addGroup(long userId, ServiceContext serviceContext) {
		Group group = GroupLocalServiceUtil.addGroup(
				userId,
				GroupConstants.DEFAULT_PARENT_GROUP_ID, null, 0, 0,
				HashMapBuilder.put(
						LocaleUtil.getDefault(), "Test Site Name"
				).build(),
				(Map<Locale, String>)null, GroupConstants.TYPE_SITE_OPEN, true,
				GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, null, true, true,
				serviceContext);

		return group.getGroupId();
	}

	static void addVocabularies(long userId, long groupId, ServiceContext serviceContext) {
		for (int i = 1; i <= 3; i++) {
			AssetVocabularyLocalServiceUtil.addVocabulary(
					userId, groupId,
					vocabularyName + i, serviceContext);
		}
	}

	static AssetCategory addCategory(
			long userId, long groupId, String vocabularyName, String categoryName, ServiceContext serviceContext) {
		AssetVocabulary vocabulary = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, vocabularyName);

		return AssetCategoryLocalServiceUtil.addCategory(
				userId, groupId, categoryName,
				vocabulary.getVocabularyId(), serviceContext);
	}

	static void addWebContents(
			int start, int end, long userId, long groupId, ServiceContext serviceContext) {
		for (int i = start; i <= end; i++) {
			Map<Locale, String> titleMap = HashMapBuilder.put(
					LocaleUtil.getDefault(), webContentTitle + i
			).build();

			Map<Locale, String> descriptionMap = HashMapBuilder.put(
					LocaleUtil.getDefault(), webContentDescription + i
			).build();

			JournalArticleLocalServiceUtil.addArticle(
					null, userId, groupId,
					JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID, titleMap,
					descriptionMap, webContentContent, "BASIC-WEB-CONTENT",
					"BASIC-WEB-CONTENT", serviceContext);
		}
	}

	static AssetListEntry addDynamicCollection(
			long userId, long groupId, long structureId, ServiceContext serviceContext) {
		return AssetListEntryLocalServiceUtil.addDynamicAssetListEntry(
				userId, groupId,
						"Collection Title", UnicodePropertiesBuilder.create(
						true
				).put(
						"anyAssetType",
						String.valueOf(PortalUtil.getClassNameId(JournalArticle.class))
				).put(
						"anyClassTypeJournalArticleAssetRendererFactory",
						String.valueOf(structureId)
				).put(
						"classNameIds", String.valueOf(PortalUtil.getClassNameId(JournalArticle.class))
				).put(
						"classTypeIdsJournalArticleAssetRendererFactory", String.valueOf(structureId)
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
	}

	public static void main(String[] args) {

		// Add a test site

		ServiceContext serviceContext = new ServiceContext();

		long defaultCompanyId = PortalUtil.getDefaultCompanyId();

		Company defaultCompany = CompanyLocalServiceUtil.getCompany(defaultCompanyId);

		long defaultUserId = getDefaultUserId(defaultCompanyId);

		serviceContext.setCompanyId(defaultCompanyId);

		long groupId = addGroup(defaultUserId, serviceContext);

		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(defaultUserId);
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		// Add three vocabularies

		addVocabularies(defaultUserId, groupId, serviceContext);

		// Add three categories to each vocabulary

		AssetCategory categoryA1 = addCategory(
				defaultUserId, groupId, vocabularyName + "1", categoryName + "A1", serviceContext);

		AssetCategory categoryA2 = addCategory(
				defaultUserId, groupId, vocabularyName + "1", categoryName + "A2", serviceContext);

		AssetCategory categoryA3 = addCategory(
				defaultUserId, groupId, vocabularyName + "1", categoryName + "A3", serviceContext);

		AssetCategory categoryB1 = addCategory(
				defaultUserId, groupId, vocabularyName + "2", categoryName + "B1", serviceContext);

		AssetCategory categoryB2 = addCategory(
				defaultUserId, groupId, vocabularyName + "2", categoryName + "B2", serviceContext);

		AssetCategory categoryB3 = addCategory(
				defaultUserId, groupId, vocabularyName + "2", categoryName + "B3", serviceContext);

		AssetCategory categoryC1 = addCategory(
				defaultUserId, groupId, vocabularyName + "3", categoryName + "C1", serviceContext);

		AssetCategory categoryC2 = addCategory(
				defaultUserId, groupId, vocabularyName + "3", categoryName + "C2", serviceContext);

		AssetCategory categoryC3 = addCategory(
				defaultUserId, groupId, vocabularyName + "3", categoryName + "C3", serviceContext);

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
		serviceContext1.setAddGroupPermissions(true);
		serviceContext1.setAddGuestPermissions(true);

		addWebContents(1, 25, defaultUserId, groupId, serviceContext1);

		long[] categoryIds2 = [
				categoryA1.getCategoryId(),
				categoryB1.getCategoryId(),
				categoryC1.getCategoryId()];

		ServiceContext serviceContext2 = new ServiceContext();

		serviceContext2.setCompanyId(defaultCompanyId);
		serviceContext2.setScopeGroupId(groupId);
		serviceContext2.setUserId(defaultUserId);
		serviceContext2.setAssetCategoryIds(categoryIds2);
		serviceContext2.setAddGroupPermissions(true);
		serviceContext2.setAddGuestPermissions(true);

		addWebContents(26, 50, defaultUserId, groupId, serviceContext2);

		long[] categoryIds3 = [
				categoryA1.getCategoryId(),
				categoryB2.getCategoryId(),
				categoryC2.getCategoryId()];

		ServiceContext serviceContext3 = new ServiceContext();

		serviceContext3.setCompanyId(defaultCompanyId);
		serviceContext3.setScopeGroupId(groupId);
		serviceContext3.setUserId(defaultUserId);
		serviceContext3.setAssetCategoryIds(categoryIds3);
		serviceContext3.setAddGroupPermissions(true);
		serviceContext3.setAddGuestPermissions(true);

		addWebContents(51, 75, defaultUserId, groupId, serviceContext3);

		long[] categoryIds4 = [categoryA1.getCategoryId()];

		ServiceContext serviceContext4 = new ServiceContext();

		serviceContext4.setCompanyId(defaultCompanyId);
		serviceContext4.setScopeGroupId(groupId);
		serviceContext4.setUserId(defaultUserId);
		serviceContext4.setAssetCategoryIds(categoryIds4);
		serviceContext4.setAddGroupPermissions(true);
		serviceContext4.setAddGuestPermissions(true);

		addWebContents(76, 100, defaultUserId, groupId, serviceContext4);

		// Add a dynamic collection for Web Content Article and Basic Web Content

		DDMStructure ddmStructure = DDMStructureLocalServiceUtil.getStructure(
				defaultCompany.getGroupId(), PortalUtil.getClassNameId(JournalArticle.class), "BASIC-WEB-CONTENT");

		long structureId = ddmStructure.getStructureId();

		AssetListEntry assetListEntry = addDynamicCollection(defaultUserId, groupId, structureId, serviceContext);

		// Add a content page

		Layout layout = LayoutLocalServiceUtil.addLayout(
				defaultUserId, groupId, false, LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, pageName, StringPool.BLANK, StringPool.BLANK,
				LayoutConstants.TYPE_CONTENT, false, pageFriendlyUrl,
				serviceContext);

		Layout draftLayout = layout.fetchDraftLayout();

		//Add a Collection Display to a content page

		LayoutStructure layoutStructure = new LayoutStructure();

		layoutStructure.addRootLayoutStructureItem();

		LayoutPageTemplateStructureLocalServiceUtil.addLayoutPageTemplateStructure(
				defaultUserId, groupId, draftLayout.getPlid(),
				layoutStructure.toString(),
				serviceContext);

		CollectionStyledLayoutStructureItem collectionStyledLayoutStructureItem =
				(CollectionStyledLayoutStructureItem)layoutStructure.addCollectionStyledLayoutStructureItem(
						layoutStructure.getMainItemId(), 0);

		//Select the dynamic collection in Collecton Display with Bordered List style, Numeric pagination and Display All Collection Items

		collectionStyledLayoutStructureItem.updateItemConfig(
				JSONUtil.put(
						"collection",
						JSONUtil.put(
								"classPK",
								String.valueOf(assetListEntry.getAssetListEntryId())
						).put(
								"itemType", JournalArticle.class.getName()
						).put(
								"itemSubtype", String.valueOf(structureId)
						).put(
								"classNameId",
								String.valueOf(PortalUtil.getClassNameId(AssetListEntry.class))
						).put(
								"title", "Collection Title"
						).put(
								"type", "com.liferay.item.selector.criteria.InfoListItemSelectorReturnType"
						)
				).put("showAllItems", true
				).put("listStyle", "com.liferay.journal.web.internal.info.list.renderer.JournalArticleBorderedBasicInfoListRenderer"
				).put("paginationType", "numeric"
				).put("numberOfColumns", 1
				).put("numberOfItems", 5
				).put("numberOfItemsPerPage", 5
				));

		/**
		 Add a Grid with three modules above the Collection Display
		 Add a Collection Filter to each module
		 Select the dynamic collection in the first Collection Filter and select the V1 as category filter
		 Select the dynamic collection in the second Collection Filter and select the V2 as category filter
		 Select the dynamic collection in the third Collection Filter and select the V3 as category filter
		 **/

		LayoutStructureItem rowStyledLayoutStructureItem =
				layoutStructure.addRowStyledLayoutStructureItem(
						layoutStructure.getMainItemId(), 0, 3);

		Bundle bundle = FrameworkUtil.getBundle(GroovyExecutor.class);

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceTracker<FragmentRendererTracker, FragmentRendererTracker> serviceTracker1;

		serviceTracker1 = new ServiceTracker<>(bundleContext, FragmentRendererTracker.class, null);

		serviceTracker1.open();

		Object fragmentRendererTrackerImpl = serviceTracker1.getService();

		FragmentRenderer fragmentRenderer =
				fragmentRendererTrackerImpl.getFragmentRenderer(fragmentEntryKey);

		for (int i = 0; i < 3; i++) {
			ColumnLayoutStructureItem columnLayoutStructureItem =
					(ColumnLayoutStructureItem)layoutStructure.addColumnLayoutStructureItem(
							rowStyledLayoutStructureItem.getItemId(), i);

			columnLayoutStructureItem.setSize(4);

			DefaultFragmentRendererContext defaultFragmentRendererContext = new DefaultFragmentRendererContext(null);

			FragmentEntryLink fragmentEntryLink = FragmentEntryLinkLocalServiceUtil.addFragmentEntryLink(
					defaultUserId, groupId, 0, 0, 0, draftLayout.getPlid(),
					StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
					fragmentRenderer.getConfiguration(defaultFragmentRendererContext),
					StringPool.BLANK, StringPool.BLANK, 0,
					fragmentEntryKey, serviceContext);

			LayoutStructureItem layoutStructureItem =
					layoutStructure.addFragmentStyledLayoutStructureItem(
							fragmentEntryLink.getFragmentEntryLinkId(),
							columnLayoutStructureItem.getItemId(), 0);

			JSONObject editableValuesJSONObject =
					JSONFactoryUtil.createJSONObject(
							fragmentEntryLink.getEditableValues());

			JSONObject configurationJSONObject =
					editableValuesJSONObject.getJSONObject(
							_KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR);

			List<String> targetCollections = JSONUtil.toStringList(
					configurationJSONObject.getJSONArray("targetCollections"));

			configurationJSONObject.put(
					"targetCollections",
					[collectionStyledLayoutStructureItem.getItemId()]);

			int j = i + 1;

			AssetVocabulary vocabulary = AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, vocabularyName + j);

			configurationJSONObject.put(
					"filterKey", "category"
			).put(
					"source",
					JSONUtil.put(
							"categoryTreeNodeType", "Vocabulary"
					).put(
							"title", vocabularyName + j
					).put(
							"type", "class com.liferay.asset.categories.item.selector.AssetCategoryTreeNodeItemSelectorReturnType"
					).put(
							"categoryTreeNodeId",
							String.valueOf(vocabulary.getVocabularyId())
					)
			);

			editableValuesJSONObject.put(
					_KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR,
					configurationJSONObject);

			FragmentEntryLinkLocalServiceUtil.updateFragmentEntryLink(
					fragmentEntryLink.getFragmentEntryLinkId(), editableValuesJSONObject.toString());
		}

		serviceTracker1.close();

		LayoutPageTemplateStructureLocalServiceUtil.updateLayoutPageTemplateStructureData(
				groupId, draftLayout.getPlid(), layoutStructure.toString());

		// Publish the content page

		ServiceTracker<LayoutCopyHelper, LayoutCopyHelper> serviceTracker2;

		serviceTracker2 = new ServiceTracker<>(bundleContext, LayoutCopyHelper.class, null);

		serviceTracker2.open();

		Object layoutCopyHelperImpl = serviceTracker2.getService();

		layoutCopyHelperImpl.copyLayout(draftLayout, layout);

		LayoutLocalServiceUtil.updateStatus(
				defaultUserId, draftLayout.getPlid(),
				WorkflowConstants.STATUS_APPROVED, serviceContext);

		LayoutLocalServiceUtil.updateStatus(
				defaultUserId, layout.getPlid(),
				WorkflowConstants.STATUS_APPROVED, serviceContext);

		serviceTracker2.close();
	}
}