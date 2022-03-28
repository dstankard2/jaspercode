package net.sf.jaspercode.api.snapshot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public abstract class ItemSnapshot {

	private int id;
	private String name;

}

