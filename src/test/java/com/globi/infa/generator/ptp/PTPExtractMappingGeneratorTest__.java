package com.globi.infa.generator.ptp;
/*package com.globi.infa.generator.ptp;

import static com.globi.infa.generator.StaticObjectMother.getCCColumn;
import static com.globi.infa.generator.StaticObjectMother.getIntegrationIdColumn;
import static com.globi.infa.generator.StaticObjectMother.getNormalColumn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.globi.infa.InfaConfig;
import com.globi.infa.datasource.core.DataTypeMapper;
import com.globi.infa.datasource.core.MetadataFactoryMapper;
import com.globi.infa.datasource.core.SourceMetadataFactory;
import com.globi.infa.datasource.core.TableColumnMetadataVisitor;
import com.globi.infa.datasource.core.TableColumnRepository;
import com.globi.infa.datasource.core.TableMetadataVisitor;
import com.globi.infa.datasource.core.TableRepository;
import com.globi.infa.generator.DefaultGeneratorContext;
import com.globi.infa.generator.GeneratorContext;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.infa.workflow.PTPWorkflowSourceColumn;
import com.globi.metadata.sourcesystem.SourceSystem;
import com.nitorcreations.junit.runners.NestedRunner;

@RunWith(NestedRunner.class)
public class PTPExtractMappingGeneratorTest {

	private static final String SEARCH_TERM = "itl";

	private TableRepository tableRepository;
	private TableColumnRepository colRepository;
	private TableMetadataVisitor tableQueryVisitor;
	private TableColumnMetadataVisitor columnQueryVisitor;
	private DataTypeMapper dataTypeMapper;
	private DataTypeMapper sourceToTargetDatatypeMapper;
	private SourceSystem source;
	private InfaWorkflow inputWF;
	private Jaxb2Marshaller marshaller;
	private GeneratorContext context;
	private MetadataFactoryMapper mapper;
	private Map<String, SourceMetadataFactory> metadataMap;
	private SourceMetadataFactory metadataFactory;
	
	
	PTPWorkflow ptpWorkflow;
	static final String sourceTable = "S_PARTY_PER";
	static final String srcName = "CUK";
	
	private PTPExtractMappingGenerator service;

	@Before
	public void setUp() {
		tableRepository = mock(TableRepository.class);
		colRepository = mock(TableColumnRepository.class);
		tableQueryVisitor = mock(TableMetadataVisitor.class);
		columnQueryVisitor = mock(TableColumnMetadataVisitor.class);
		dataTypeMapper=mock(DataTypeMapper.class);
		sourceToTargetDatatypeMapper=mock(DataTypeMapper.class);
		source=	mock(SourceSystem.class);
		marshaller=	new InfaConfig().jaxb2Marshaller();
		mapper=mock(MetadataFactoryMapper.class);
		metadataFactory=mock(SourceMetadataFactory.class);
		

		when(metadataFactory.createTableColumnMetadataVisitor()).thenReturn(columnQueryVisitor);
		
		metadataMap=new HashMap<>();
		metadataMap.put("CUK",metadataFactory);
		
		when(mapper.getMetadataFactoryMap()).thenReturn(metadataMap);
		
		List<PTPWorkflowSourceColumn> cols=new ArrayList<>();
		cols.add(getIntegrationIdColumn("ROW_ID"));
		cols.add(getNormalColumn("ROW_ID"));
		cols.add(getCCColumn("LAST_UPD"));

		when(metadataFactory.createSourceToTargetDatatypeMapper()).thenReturn(sourceToTargetDatatypeMapper);	
		
		ptpWorkflow = PTPWorkflow.builder()//
				.sourceName(srcName)//
				.columns(cols)
				.sourceTableName(sourceTable)//
				.workflowUri("/GeneratedWorkflows/Repl/" + "PTP_" + source+ "_"+ sourceTable  + ".xml")
				.workflowName("PTP_" + source+ "_"+ sourceTable)
				.targetTableName(source + "_" + sourceTable)
				.build();
		
		
		context=DefaultGeneratorContext.contextFor("GEN", mapper, ptpWorkflow);
		
		service = new PTPExtractMappingGenerator(repository);
	}

	public class FindBySearchTerm {

		private final int PAGE_NUMBER = 1;
		private final int PAGE_SIZE = 5;
		private final String SORT_PROPERTY = "title";

		private Pageable pageRequest;

		@Before
		public void createPageRequest() {
			Sort sort = new Sort(Sort.Direction.ASC, SORT_PROPERTY);
			pageRequest = new PageRequest(PAGE_NUMBER, PAGE_SIZE, sort);

			Page<Todo> emptyPage = new PageBuilder<Todo>().elements(new ArrayList<>()).pageRequest(pageRequest)
					.totalElements(0).build();
			given(repository.findAll(isA(Specification.class), eq(pageRequest))).willReturn(emptyPage);
		}

		@Test
		public void shouldReturnPageWithRequestedPageNumber() {
			Page<TodoDTO> searchResultPage = service.findBySearchTerm(SEARCH_TERM, pageRequest);
			assertThat(searchResultPage.getNumber()).isEqualTo(PAGE_NUMBER);
		}

		@Test
		public void shouldReturnPageWithRequestedPageSize() {
			Page<TodoDTO> searchResultPage = service.findBySearchTerm(SEARCH_TERM, pageRequest);
			assertThat(searchResultPage.getSize()).isEqualTo(PAGE_SIZE);
		}

		@Test
		public void shouldReturnPageThatIsSortedInAscendingOrderByUsingSortProperty() {
			Page<TodoDTO> searchResultPage = service.findBySearchTerm(SEARCH_TERM, pageRequest);
			assertThat(searchResultPage.getSort().getOrderFor(SORT_PROPERTY).getDirection())
					.isEqualTo(Sort.Direction.ASC);
		}

		public class WhenNoTodoEntriesAreFound {

			@Before
			public void returnZeroTodoEntries() {
				Page<Todo> emptyPage = new PageBuilder<Todo>().elements(new ArrayList<>()).pageRequest(pageRequest)
						.totalElements(0).build();
				given(repository.findAll(isA(Specification.class), eq(pageRequest))).willReturn(emptyPage);
			}

			@Test
			public void shouldReturnEmptyPage() {
				Page<TodoDTO> searchResultPage = service.findBySearchTerm(SEARCH_TERM, pageRequest);
				assertThat(searchResultPage).isEmpty();
			}

			@Test
			public void shouldReturnPageWithTotalElementCountZero() {
				Page<TodoDTO> searchResultPage = service.findBySearchTerm(SEARCH_TERM, pageRequest);
				assertThat(searchResultPage.getTotalElements()).isEqualTo(0);
			}
		}

		public class WhenOneTodoEntryIsFound {

			private final String CREATED_BY_USER = "createdByUser";
			private final String CREATION_TIME = "2014-12-24T22:28:39+02:00";
			private final String DESCRIPTION = "description";
			private final Long ID = 20L;
			private final String MODIFIED_BY_USER = "modifiedByUser";
			private final String MODIFICATION_TIME = "2014-12-24T22:29:05+02:00";
			private final String TITLE = "title";

			@Before
			public void returnOneTodoEntry() {
				Todo found = new TodoBuilder().createdByUser(CREATED_BY_USER).creationTime(CREATION_TIME)
						.description(DESCRIPTION).id(ID).modifiedByUser(MODIFIED_BY_USER)
						.modificationTime(MODIFICATION_TIME).title(TITLE).build();

				Page<Todo> resultPage = new PageBuilder<Todo>().elements(Arrays.asList(found)).pageRequest(pageRequest)
						.totalElements(1).build();

				given(repository.findAll(isA(Specification.class), eq(pageRequest))).willReturn(resultPage);
			}

			@Test
			public void shouldReturnPageThatHasOneTodoEntry() {
				Page<TodoDTO> searchResultPage = service.findBySearchTerm(SEARCH_TERM, pageRequest);
				assertThat(searchResultPage.getNumberOfElements()).isEqualTo(1);
			}

			@Test
			public void shouldReturnPageThatHasCorrectInformation() {
				TodoDTO found = service.findBySearchTerm(SEARCH_TERM, pageRequest).getContent().get(0);

				assertThatTodoDTO(found).hasId(ID).hasTitle(TITLE).hasDescription(DESCRIPTION)
						.wasCreatedAt(CREATION_TIME).wasCreatedByUser(CREATED_BY_USER).wasModifiedAt(MODIFICATION_TIME)
						.wasModifiedByUser(MODIFIED_BY_USER);
			}

			@Test
			public void shouldReturnPageWithTotalElementCountOne() {
				Page<TodoDTO> searchResultPage = service.findBySearchTerm(SEARCH_TERM, pageRequest);
				assertThat(searchResultPage.getTotalElements()).isEqualTo(1);
			}
		}
	}
}*/