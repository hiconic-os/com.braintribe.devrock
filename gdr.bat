@echo off
setlocal EnableDelayedExpansion

REM Create an empty variable to hold the Gradle arguments
set GRADLE_ARGS=

REM Create a flag to check if we're still in the properties
set PROP_FLAG=true

REM Loop over all arguments
:loop
if "%~1"=="" goto afterloop
set _key=%~1
set _val=%~2

REM Check if we've hit the -- argument
if "!_key!"=="--" (
    set PROP_FLAG=false
    REM Shift to the next argument
    shift /1
    goto loop
)

REM Check if the argument starts with --
if "!PROP_FLAG!"=="true" (
    if "!_key:~0,2!"=="--" (
        REM Remove the -- from the argument name
        set _key=!_key:~2!
        REM Add to the Gradle arguments
        set GRADLE_ARGS=!GRADLE_ARGS! -P!_key!=!_val!
        REM Shift twice to skip over the value we just processed
        shift /1
    )
)

REM Shift to the next argument
shift /1
goto loop

:afterloop
REM Call Gradle with the translated arguments
rem echo gradle !GRADLE_ARGS! %*
echo gradle !GRADLE_ARGS!
endlocal