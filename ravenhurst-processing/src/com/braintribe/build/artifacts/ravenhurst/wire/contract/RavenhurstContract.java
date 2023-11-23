package com.braintribe.build.artifacts.ravenhurst.wire.contract;

import javax.sql.DataSource;

import com.braintribe.build.ravenhurst.scanner.Scanner;
import com.braintribe.wire.api.space.WireSpace;

public interface RavenhurstContract extends WireSpace {

	Scanner scanner();
	DataSource dataSource();

	String dateTimeFormat();

}
