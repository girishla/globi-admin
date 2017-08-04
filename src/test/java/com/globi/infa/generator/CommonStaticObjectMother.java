package com.globi.infa.generator;

import java.util.ArrayList;
import java.util.List;

import com.globi.infa.metadata.src.InfaSourceColumnDefinition;

import xjc.SOURCE;
import xjc.SOURCEFIELD;

public class CommonStaticObjectMother {
	public static List<InfaSourceColumnDefinition> getInfaSourceColumnsFromSourceDefn(SOURCE source) {

		List<InfaSourceColumnDefinition> infaSourceCols = new ArrayList<>();

		
		for (SOURCEFIELD field : source.getSOURCEFIELD()) {
			infaSourceCols.add(InfaSourceColumnDefinition.builder()//
					.columnDataType(field.getDATATYPE())//
					.columnLength(Integer.parseInt(field.getLENGTH()))//
					.columnName(field.getNAME())//
					.columnNumber(Integer.parseInt(field.getFIELDNUMBER()))//
					.columnSequence(Integer.parseInt(field.getFIELDNUMBER()))//
					.nullable(field.getNULLABLE())//
					.offset(Integer.parseInt(field.getOFFSET()))//
					.physicalLength(Integer.parseInt(field.getPHYSICALLENGTH()))//
					.physicalOffset(Integer.parseInt(field.getPHYSICALOFFSET()))//
					.precision(Integer.parseInt(field.getPRECISION()))//
					.scale(Integer.parseInt(field.getSCALE()))//
					.selected(false)//
					.build());
		}

		return infaSourceCols;

	}

}
