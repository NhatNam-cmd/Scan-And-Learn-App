package com.example.englishapp.core.mapper;

import com.example.englishapp.core.model.Vocabulary;
import com.example.englishapp.core.network.dictionary.dto.DictionaryResponseDto;
import com.example.englishapp.core.network.dictionary.dto.MeaningDto;
import com.example.englishapp.core.network.dictionary.dto.DefinitionDto;
import com.example.englishapp.core.network.dictionary.dto.PhoneticDto;

import java.util.List;

public class DictionaryMapper {

    public static Vocabulary mapToVocabulary(DictionaryResponseDto dto) {
        if (dto == null || dto.getWord() == null || dto.getWord().isEmpty()) {
            return null;
        }

        List<MeaningDto> meanings = dto.getMeanings();
        MeaningDto firstMeaning = (meanings != null && !meanings.isEmpty())
                ? meanings.get(0)
                : null;

        DefinitionDto firstDefinition = null;
        if (firstMeaning != null) {
            List<DefinitionDto> definitions = firstMeaning.getDefinitions();
            if (definitions != null && !definitions.isEmpty()) {
                firstDefinition = definitions.get(0);
            }
        }

        // Lấy phonetic
        String phonetic = dto.getPhonetic() != null ? dto.getPhonetic() : "";
        List<PhoneticDto> phonetics = dto.getPhonetics();
        if (phonetics != null && !phonetics.isEmpty()) {
            for (PhoneticDto p : phonetics) {
                if (p.getText() != null && !p.getText().isEmpty()) {
                    phonetic = p.getText();
                    break;
                }
            }
        }

        // Lấy audio URL
        String audioUrl = "";
        if (phonetics != null && !phonetics.isEmpty()) {
            for (PhoneticDto p : phonetics) {
                if (p.getAudio() != null && !p.getAudio().isEmpty()) {
                    audioUrl = p.getAudio();
                    break;
                }
            }
        }

        Vocabulary vocabulary = new Vocabulary(
                dto.getWord(),
                firstDefinition != null ? firstDefinition.getDefinition() : "No definition found",
                "SCAN"
        );
        vocabulary.setPhonetic(phonetic);
        vocabulary.setExampleSentence(firstDefinition != null && firstDefinition.getExample() != null
                ? firstDefinition.getExample()
                : "");
        vocabulary.setPartOfSpeech(firstMeaning != null ? firstMeaning.getPartOfSpeech() : "");
        vocabulary.setAudioUrl(audioUrl);

        return vocabulary;
    }
}