package com.globi.infa.metadata.src;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
@Setter
public class SILInfaSourceColumnDefinition extends InfaSourceColumnDefinition {

	private String columnType;
	private Boolean targetColumnFlag = false;
	private Boolean legacyColumnFlag = false;
	private Boolean miniDimColumnFlag = false;
	private Boolean domainLookupColumnFlag = false;
	private Boolean autoColumnFlag = false;
	private String dimTableName = "";

	public static Builder builder() {
		return new Builder();
	}

	public Boolean isAmountColumn() {

		if (this.getColumnName().startsWith("DOC_")) {
			return true;
		}
		
		return false;

	}

	public static class Builder extends InfaSourceColumnDefinition.Builder<Builder> {

		private String columnType;
		private Boolean targetColumnFlag = false;
		private Boolean legacyColumnFlag = false;
		private Boolean miniDimColumnFlag = false;
		private Boolean domainLookupColumnFlag = false;
		private Boolean autoColumnFlag = false;
		private String dimTableName = "";

		Builder() {

		}

		public Builder columnType(String columnType) {
			this.columnType = columnType;
			return this;

		}

		public Builder targetColumnFlag(Boolean targetColumnFlag) {
			this.targetColumnFlag = targetColumnFlag;
			return this;

		}

		public Builder legacyColumnFlag(Boolean legacyColumnFlag) {
			this.legacyColumnFlag = legacyColumnFlag;
			return this;

		}

		public Builder miniDimColumnFlag(Boolean miniDimColumnFlag) {
			this.miniDimColumnFlag = miniDimColumnFlag;
			return this;

		}

		public Builder domainLookupColumnFlag(Boolean domainLookupColumnFlag) {
			this.domainLookupColumnFlag = domainLookupColumnFlag;
			return this;

		}

		public Builder autoColumnFlag(Boolean autoColumnFlag) {
			this.autoColumnFlag = autoColumnFlag;
			return this;

		}

		public Builder dimTableName(String dimTableName) {
			this.dimTableName = dimTableName;
			return this;

		}

		public SILInfaSourceColumnDefinition build() {

			return new SILInfaSourceColumnDefinition(this);

		}

	}

	protected SILInfaSourceColumnDefinition(Builder builder) {

		super(builder);
		targetColumnFlag = builder.targetColumnFlag;
		legacyColumnFlag = builder.legacyColumnFlag;
		miniDimColumnFlag = builder.miniDimColumnFlag;
		domainLookupColumnFlag = builder.domainLookupColumnFlag;
		autoColumnFlag = builder.autoColumnFlag;
		columnType = builder.columnType;
		dimTableName = builder.dimTableName;

	}

}
