REM Malaclypse copy batch
set MODEL=%BT__ARTIFACTS_HOME%\com\braintribe\model\ArtifactModel\2.1\dist\lib\ArtifactModel-2.1.jar
REM AC
copy %MODEL% %BT__ARTIFACTS_HOME%\com\braintribe\build\artifacts\ArtifactContainer\2.1\lib
REM GREYFACE
copy %MODEL% %BT__ARTIFACTS_HOME%\com\braintribe\build\artifacts\Greyface\1.2\lib
