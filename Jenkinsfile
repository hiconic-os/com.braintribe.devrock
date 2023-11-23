@Library('main') _
withUsernamePasswordCredentialsMaskingSecurePasswordsOnly(credentialsId: 'artifactory-devrock-tests-read', usernameVariable: 'ARTIFACTORY_DEVROCK_TESTS_READ_USERNAME', passwordVariable: 'ARTIFACTORY_DEVROCK_TESTS_READ_PASSWORD') {
	withUsernamePasswordCredentialsMaskingSecurePasswordsOnly(credentialsId: 'artifactory-devrock-tests-write', usernameVariable: 'ARTIFACTORY_DEVROCK_TESTS_WRITE_USERNAME', passwordVariable: 'ARTIFACTORY_DEVROCK_TESTS_WRITE_PASSWORD') {
		runCiPipeline(
			jenkinsfile: this,
			projectBaseName: 'core',
			ciNotificationChannel: '#pd-cxz-cicd',
			artifactsWhichNeedToBeRepublishedWhenSolutionListChanges: ['ravenhurst'],
			unitTestsEnvironmentVariables: [
				DEVROCK_TESTS_REPOSITORY_BASE_URL: globals.artifactoryBaseUrl(),
				DEVROCK_TESTS_RAVENHURST_BASE_URL: globals.ravenhurstBaseUrl() + "/rest/",
				DEVROCK_TESTS_READ_USERNAME: ARTIFACTORY_DEVROCK_TESTS_READ_USERNAME,
				DEVROCK_TESTS_READ_PASSWORD: ARTIFACTORY_DEVROCK_TESTS_READ_PASSWORD,
				DEVROCK_TESTS_WRITE_USERNAME: ARTIFACTORY_DEVROCK_TESTS_WRITE_USERNAME,
				DEVROCK_TESTS_WRITE_PASSWORD: ARTIFACTORY_DEVROCK_TESTS_WRITE_PASSWORD
			]
		)
	}
}
