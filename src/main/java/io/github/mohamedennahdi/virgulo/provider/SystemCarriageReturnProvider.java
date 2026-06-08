package io.github.mohamedennahdi.virgulo.provider;

import java.util.Objects;

public class SystemCarriageReturnProvider implements CarriageReturnProvider {

	@Override
	public String get() {
		String os = System.getProperty("os.name");
        return Objects.nonNull(os) && os.contains("Windows") ? "\r\n" : "\n";
	}

}
