# Sphinx Speech Recognition Service

## Speech recognition app

The application allows you to recognize the commands specified in the configuration file, and broadcasts them other applications. Activation of the recognition mode is performed using a key phrase that is set in the application settings. For recognition, use the library [Sphinx](https://cmusphinx.github.io/).

## How to build
The build requires Android SDK (version of Android 5.1) and JDK (with configured environment variables JAVA_HOME and ANDROID_HOME).
Run `gradlew build` to build an executable apk.  
The apk will be in `app/build/outputs/apk` directory.

## Reqiured permission:
  - WRITE_EXTERNAL_STORAGE
  - RECORD_AUDIO
  - VIBRATE

## Additional resources
The application requires an acoustic model and a command grammar description file (in JSGF format).
Acoustic models for the required language can be downloaded for example [here](https://sourceforge.net/projects/cmusphinx/files/Acoustic%20and%20Language%20Models/).
These files must be placed in the phone’s memory and specify the path to the corresponding folder in the application settings.
The contents of this folder should look like this (depending on the model used):
  - ptm
    - feat.params
    - feature_transform
    - mdef
    - means
    - mixture_weights
    - noisedict
    - transition_matrices
    - variances
  - command.gram
  - dict.dic

You must write the command.gram file yourself according to the [JSGF](https://www.w3.org/TR/2000/NOTE-jsgf-20000605/) standard.
Sample file:
```
#JSGF V1.0;
grammar commands;
<commands> = enable | disable | left | right | forward | backwards;
<equipments> = light;
public <command> = <commands> [<equipments>];
```
The dictionary (file dict.dic) can be used complete for the selected language or limited only by the words used in the commands and key phrase

## Broadcast Format
Each message contains the following fields:
 - status (required)
 - data (optional)
 - confidence (optional)

The "status" field can have the following values:
 - "start_init" – sent when the service starts and reports the start of recognition initialization;
 - "error_init" – sent when the initialization has completed with an error (the service stops its work, the "data" field contains a description of the error);
 - "init_complet"» – sent after successful completion of recognition initialization (after this, recognition of the key phrase is automatically started);
 - "start_recognize_keyphrase" – sent when the key phrase recognition process is running;
 - "start_recognize_command" – sent when command recognition is running;
 - "keyphrase_recognized" – sent when the key phrase is recognized (it is not sent to the “data” field, but if necessary, this behavior can be changed in the next version);
 - "command_recognized" – sent when a command is recognized (the recognized command is contained in the “data” field);
 - "recognize_command_timeout" – sent when the command is not recognized and the service has returned to the expectation of a key phrase based on the timeout specified in the settings;
 - "stop" – sent when the service is stopped and stops performing voice recognition.

The “data” field is sent under the following statuses: “error_init” and “command_recognized”.
In the first case, the field contains a description of the initialization error. Often, initialization errors are related to the lack of acoustic model, dictionary and / or grammar files or their incorrect naming. Errors in the grammar file are also possible (syntax errors or the absence of a word in the dictionary).

In the second case, the field contains the recognized command.

The “confidence” field is sent only when the status is “command_recognized” and contains values of trust for the recognized command. With values from -2 to 0, the command is recognized almost perfectly. False commands are rarely recognized with this value. With values from -3 to -2 there is a high probability that the command is recognized incorrectly. At values below -3 it is almost guaranteed that the recognition is wrong. It is worth considering that these boundaries are indicated for commands consisting of at least two words. For a commad of one word, the value of trust will often be lower than -2, although it will be recognized almost every time.

## Application language
The application is currently available only in Russian.
