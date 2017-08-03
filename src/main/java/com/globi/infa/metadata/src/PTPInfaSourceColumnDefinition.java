package com.globi.infa.metadata.src;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@ToString(callSuper = true)
@Getter
@Setter
public class PTPInfaSourceColumnDefinition extends InfaSourceColumnDefinition {

	private Boolean integrationIdFlag = false;
	private Boolean pguidFlag = false;
	private Boolean buidFlag = false;
	private Boolean ccFlag = false;

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder extends InfaSourceColumnDefinition.Builder<Builder> {
	

		private Boolean integrationIdFlag = false;
		private Boolean pguidFlag = false;
		private Boolean buidFlag = false;
		private Boolean ccFlag = false;

		Builder() {
			
			
		}

		public Builder integrationIdFlag(Boolean integrationIdFlag) {
			this.integrationIdFlag = integrationIdFlag;
			return this;

		}

		public Builder pguidFlag(Boolean pguidFlag) {
			this.pguidFlag = pguidFlag;
			return this;

		}
		public Builder buidFlag(Boolean buidFlag) {
			this.buidFlag = buidFlag;
			return this;

		}
		

		public Builder ccFlag(Boolean ccFlag) {
			this.ccFlag = ccFlag;
			return this;

		}

		public PTPInfaSourceColumnDefinition build() {

			return new PTPInfaSourceColumnDefinition(this); 
			
		}

	
	}
	
	
	protected PTPInfaSourceColumnDefinition(Builder builder) {
        
		super(builder);
		integrationIdFlag=builder.integrationIdFlag;
		pguidFlag =builder.pguidFlag;
		buidFlag =builder.buidFlag;
		ccFlag=builder.ccFlag;

    }
	
	
	

}
