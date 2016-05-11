<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:hello2types="http://apache.org/hello2_world_soap_http/types"
	xmlns:hellotypes="http://apache.org/hello_world_soap_http/types">

	<xsl:template match="/">
		<hellotypes:greetMeResponse>
			<hellotypes:requestType>
				<xsl:value-of select="//hello2types:requestType" />
			</hellotypes:requestType>
		</hellotypes:greetMeResponse>
	</xsl:template>

</xsl:stylesheet>