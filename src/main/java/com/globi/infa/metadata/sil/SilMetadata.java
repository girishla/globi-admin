package com.globi.infa.metadata.sil;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SilMetadata {

	@Builder.Default
	private String tableName = "";

	@Builder.Default
	private String stageName = "";

	@Builder.Default
	private String columnOrder = "";

	@Builder.Default
	private String columnName = "";

	@Builder.Default
	private String columnType = "";
	@Builder.Default
	private String columnDataType = "";
	
	@Builder.Default
	private int columnPrecision = 0;

	@Builder.Default
	private boolean stageColumnFlag = false;

	@Builder.Default
	private Boolean targetColumnFlag = false;

	@Builder.Default
	private Boolean legacyColumnFlag = false;

	@Builder.Default
	private Boolean miniDimColumnFlag = false;
	@Builder.Default
	private Boolean domainLookupColumnFlag = false;
	@Builder.Default
	private Boolean autoColumnFlag = false;
	@Builder.Default
	private String dimTableName = "";

}
