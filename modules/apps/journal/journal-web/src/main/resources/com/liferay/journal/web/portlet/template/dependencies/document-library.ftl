<#include "init.ftl">

<#assign
	localeVariable = "locale"

	labelName = "languageUtil.format(" + localeVariable + ", \"download-x\", \"" + label + "\", false)"
/>

${r"<#if"} (${variableName})?? && (${labelName})??>
	<a href="${getVariableReferenceCode(variableName)}">
		${getVariableReferenceCode(labelName)}
	</a>
${r"</#if>"}