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
import java.util.Map;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.layout.util.structure.CollectionStyledLayoutStructureItem;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.layout.util.LayoutCopyHelper;

public class LayoutStructureTest {
	public static void main(String[] args) {
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
						LocaleUtil.getDefault(), "New Site Name"
				).build(),
				(Map<Locale, String>)null, GroupConstants.TYPE_SITE_OPEN, true,
				GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, null, true, true,
				serviceContext);

		long groupId = group.getGroupId();

		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(defaultUserId);

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

		String pageName = "Test Page Name";
		String pageFriendlyUrl = "/test-page-name";

		// Add a content page

		Layout layout = LayoutLocalServiceUtil.addLayout(
				defaultUserId, groupId, false, LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, pageName, StringPool.BLANK, StringPool.BLANK,
				LayoutConstants.TYPE_CONTENT, false, pageFriendlyUrl,
				serviceContext);

		LayoutStructure layoutStructure = new LayoutStructure();

		layoutStructure.addRootLayoutStructureItem();

		LayoutPageTemplateStructureLocalServiceUtil.addLayoutPageTemplateStructure(
				defaultUserId, groupId, layout.getPlid(),
				layoutStructure.toString(),
				serviceContext);

		CollectionStyledLayoutStructureItem collectionStyledLayoutStructureItem =
				(CollectionStyledLayoutStructureItem)layoutStructure.addCollectionStyledLayoutStructureItem(
						layoutStructure.getMainItemId(), 0);

		collectionStyledLayoutStructureItem.setCollectionJSONObject(
				JSONUtil.put(
						"classNameId",
						PortalUtil.getClassNameId(AssetListEntry.class)
				).put(
						"classPK", assetListEntry.getAssetListEntryId()
				).put(
						"itemType", JournalArticle.class
				).put(
						"itemSubtype", PortalUtil.getClassNameId(JournalArticle.class)
				).put(
						"classPK", assetListEntry.getAssetListEntryId()
				));

		LayoutPageTemplateStructureLocalServiceUtil.updateLayoutPageTemplateStructureData(
				groupId, layout.getPlid(), layoutStructure.toString());

// Layout draftLayout = LayoutLocalServiceUtil.fetchLayout(
// PortalUtil.getClassNameId(Layout.class), layout.getPlid());
//
// LayoutCopyHelper.copyLayout(draftLayout, layout);
//
// LayoutLocalServiceUtil.updateStatus(
// defaultUserId, draftLayout.getPlid(),
// WorkflowConstants.STATUS_APPROVED, serviceContext);
//
// LayoutLocalServiceUtil.updateStatus(
// defaultUserId, layout.getPlid(),
// WorkflowConstants.STATUS_APPROVED, serviceContext);
	}
}