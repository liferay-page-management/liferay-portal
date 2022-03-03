userId = com.liferay.portal.kernel.security.permission.PermissionThreadLocal.getPermissionChecker().getUser().getUserId();

companyId = com.liferay.portal.kernel.security.auth.CompanyThreadLocal.getCompanyId();

groupId = com.liferay.portal.kernel.service.GroupLocalServiceUtil.getGroup(companyId, "Test Site Name").getGroupId();

name = "Test Groovy Vocabulary";

serviceContext = com.liferay.portal.kernel.service.ServiceContextFactory.getInstance(actionRequest);

vocabulary = com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil.addVocabulary(userId, groupId, name, name, null, null, null, serviceContext);

out.println("vocabulary name: " + vocabulary.getName());

try {
vocabulary = com.liferay.asset.kernel.service.AssetVocabularyLocalServiceUtil.getGroupVocabulary(groupId, name);
out.println(vocabulary);
} catch(Exception e) {
out.println(e.getMessage());
}