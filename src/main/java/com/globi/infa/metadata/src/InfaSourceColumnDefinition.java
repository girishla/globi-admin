package com.globi.infa.metadata.src;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.globi.infa.AbstractEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString(callSuper = true)
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "M_INFA_SRC_DEFN_COLS")
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class InfaSourceColumnDefinition extends AbstractEntity {

	@NonNull
	private String columnName;
	@NonNull
	private String columnDataType;
	private int columnNumber;
	private String nullable;
	private int columnLength;
	private int offset;
	private int physicalLength;
	private int physicalOffset;
	private int precision;
	private int scale;
	private int columnSequence = 0;

	@Transient
	private Boolean selected = false;

	public static Builder builder() {
		return new Builder();
	}

	
	//Builder design to support Derived classes based on Curiously Recurring Generic Pattern - http://www.artima.com/weblogs/viewpost.jsp?thread=133275
	public static class Builder<T extends Builder> {
		private String columnName;
		private String columnDataType;
		private int columnNumber;
		private String nullable;
		private int columnLength;
		private int offset;
		private int physicalLength;
		private int physicalOffset;
		private int precision;
		private int scale;
		private int columnSequence = 0;

		@Transient
		private Boolean selected = false;

		Builder() {
		}

		public T columnName(String columnName) {
			this.columnName = columnName;
			return (T) this;

		}

		public T columnDataType(String columnDataType) {
			this.columnDataType = columnDataType;
			return (T) this;

		}

		public T columnNumber(int columnNumber) {
			this.columnNumber = columnNumber;
			return (T) this;

		}

		public T nullable(String nullable) {
			this.nullable = nullable;
			return (T) this;

		}

		public T columnLength(int columnLength) {
			this.columnLength = columnLength;
			return (T) this;

		}

		public T offset(int offset) {
			this.offset = offset;
			return (T) this;

		}

		public T physicalLength(int physicalLength) {
			this.physicalLength = physicalLength;
			return (T) this;

		}

		public T physicalOffset(int physicalOffset) {
			this.physicalOffset = physicalOffset;
			return (T) this;

		}

		public T precision(int precision) {
			this.precision = precision;
			return (T) this;

		}

		public T scale(int scale) {
			this.scale = scale;
			return (T) this;

		}

		public T columnSequence(int columnSequence) {
			this.columnSequence = columnSequence;
			return (T) this;

		}

		public T selected(Boolean selected) {
			this.selected = selected;
			return (T) this;

		}

		public InfaSourceColumnDefinition build() {

			return new InfaSourceColumnDefinition(columnName, columnDataType, columnNumber, nullable, columnLength,
					offset, physicalLength, physicalOffset, precision, scale, columnSequence, selected);
		}

		@java.lang.Override
		public String toString() {
			return "InfaSourceColumnDefinition.Builder(columnName = " + this.columnName + ", columnDataType = "
					+ this.columnDataType + ", columnNumber = " + this.columnNumber + ")";
		}
	}

	protected InfaSourceColumnDefinition(Builder builder) {

		columnName = builder.columnName;
		columnDataType = builder.columnDataType;
		columnNumber = builder.columnNumber;
		nullable = builder.nullable;
		columnLength = builder.columnLength;
		offset = builder.offset;
		physicalLength = builder.physicalLength;
		physicalOffset = builder.physicalOffset;
		precision = builder.precision;
		scale = builder.scale;
		columnSequence = builder.columnSequence;

	}

}
