package net.sf.jaspercode.api.snapshot;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SystemAttributeSnapshot {

	@Builder.Default
	private String name = null;
	@Builder.Default
	private String type = null;
	@Builder.Default
	private String description = null;
	@Builder.Default
	private List<Integer> originators = new ArrayList<>();
	@Builder.Default
	private List<Integer> dependants = new ArrayList<>();

}

