<#include "init.ftl">

<#assign variableName = name + ".getFriendlyUrl()" />

${r"<#if"} (${variableName})??>
	<a data-senna-off="true" href="${getVariableReferenceCode(variableName)}">
		${label}
	</a>
${r"</#if>"}