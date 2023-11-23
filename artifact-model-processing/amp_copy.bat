REM Malaclypse copy batch
set MALACLYPSE="%BT__ARTIFACTS_HOME%\com\braintribe\model\processing\ArtifactModelProcessing\1.2\dist\lib\ArtifactModelProcessing-1.2.jar"
REM AC
copy %MALACLYPSE% %BT__ARTIFACTS_HOME%\com\braintribe\build\artifacts\ArtifactContainer\2.2\lib
REM GREYFACE
copy %MALACLYPSE% %BT__ARTIFACTS_HOME%\com\braintribe\build\artifacts\Greyface\1.2\lib
