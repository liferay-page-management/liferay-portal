import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.petra.string.StringPool;

class AddLayouts {
	static String pageName = "Test Page Name";
	static String pageFriendlyUrl = "/test-page-name";

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

	public static void main(String[] args) {
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

		for (int i = 1; i <= 500; i++) {
			LayoutLocalServiceUtil.addLayout(
					defaultUserId, groupId, false, LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, pageName + i, StringPool.BLANK, StringPool.BLANK,
					LayoutConstants.TYPE_PORTLET, false, pageFriendlyUrl + "-" + i,
					serviceContext);
		}
	}

}