package net.sf.jaspercode.api.snapshot;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ComponentSnapshot extends ItemSnapshot {

	@Builder.Default
	private List<String> systemAttributesOriginated = new ArrayList<>();
	@Builder.Default
	private List<String> systemAttributeDependencies = new ArrayList<>();

	// Source files (paths)
	@Builder.Default
	private List<String> sourceFilePaths = new ArrayList<>();

	@Builder.Default
	private List<TypeInfo> typesOriginated = new ArrayList<>();
	@Builder.Default
	private List<TypeInfo> typeDependencies = new ArrayList<>();

	@Builder.Default
	private List<String> objectDependencies = new ArrayList<>();
	
	@Builder.Default
	private List<String> componentsOriginated = new ArrayList<>();

}

