package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.postgres.ReportGlobalVars;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportGlobalVarsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportGlobalVarsService Unit Tests")
class ReportGlobalVarsServiceTest {

    @Mock
    private ReportGlobalVarsRepository globalVarsRepository;

    @InjectMocks
    private ReportGlobalVarsService globalVarsService;

    private List<ReportGlobalVars> testVars;

    @BeforeEach
    void setUp() {
        testVars = Arrays.asList(
                createVar("KEY1", "value1"),
                createVar("KEY2", "value2"),
                createVar("KEY3", "value3")
        );
    }

    private ReportGlobalVars createVar(String key, String value) {
        ReportGlobalVars var = new ReportGlobalVars();
        var.setKey(key);
        var.setValue(value);
        return var;
    }

    /**
     * Проверяет, что кэш инициализируется при старте приложения.
     * Ожидается: кэш заполнен данными из БД
     */
    @Test
    @DisplayName("Should initialize cache on application start")
    void shouldInitializeCacheOnApplicationStart() {
        when(globalVarsRepository.findAll()).thenReturn(testVars);

        globalVarsService.init();

        Map<String, String> cache = ReportGlobalVarsService.getGlobalVarsCache();
        assertThat(cache)
                .hasSize(3)
                .containsEntry("KEY1", "value1")
                .containsEntry("KEY2", "value2")
                .containsEntry("KEY3", "value3");

        verify(globalVarsRepository, times(1)).findAll();
    }

    /**
     * Проверяет, что при сохранении с дубликатами ключей выбрасывается исключение.
     * Ожидается: IllegalArgumentException
     */
    @Test
    @DisplayName("Should throw exception when saving vars with duplicate keys")
    void shouldThrowExceptionWhenSavingVarsWithDuplicateKeys() {
        List<ReportGlobalVars> varsWithDuplicates = Arrays.asList(
                createVar("KEY1", "value1"),
                createVar("KEY1", "value2"),
                createVar("KEY2", "value3")
        );

        assertThatThrownBy(() -> globalVarsService.saveAllVars(varsWithDuplicates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("В списке есть дубликаты ключей");

        verify(globalVarsRepository, never()).deleteAllInBatch();
        verify(globalVarsRepository, never()).saveAll(anyList());
    }

    /**
     * Проверяет, что при сохранении с null ключом выбрасывается исключение.
     * Ожидается: IllegalArgumentException
     */
    @Test
    @DisplayName("Should throw exception when saving vars with null key")
    void shouldThrowExceptionWhenSavingVarsWithNullKey() {
        List<ReportGlobalVars> varsWithNullKey = Arrays.asList(
                createVar(null, "value1"),
                createVar("KEY2", "value2")
        );

        assertThatThrownBy(() -> globalVarsService.saveAllVars(varsWithNullKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ключ переменной не может быть null или пустым");

        verify(globalVarsRepository, never()).deleteAllInBatch();
        verify(globalVarsRepository, never()).saveAll(anyList());
    }

    /**
     * Проверяет, что при сохранении с пустым ключом выбрасывается исключение.
     * Ожидается: IllegalArgumentException
     */
    @Test
    @DisplayName("Should throw exception when saving vars with empty key")
    void shouldThrowExceptionWhenSavingVarsWithEmptyKey() {
        List<ReportGlobalVars> varsWithEmptyKey = Arrays.asList(
                createVar("", "value1"),
                createVar("KEY2", "value2")
        );

        assertThatThrownBy(() -> globalVarsService.saveAllVars(varsWithEmptyKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ключ переменной не может быть null или пустым");

        verify(globalVarsRepository, never()).deleteAllInBatch();
        verify(globalVarsRepository, never()).saveAll(anyList());
    }

    /**
     * Проверяет, что при сохранении с ключом из пробелов выбрасывается исключение.
     * Ожидается: IllegalArgumentException
     */
    @Test
    @DisplayName("Should throw exception when saving vars with whitespace key")
    void shouldThrowExceptionWhenSavingVarsWithWhitespaceKey() {
        List<ReportGlobalVars> varsWithWhitespaceKey = Arrays.asList(
                createVar("   ", "value1"),
                createVar("KEY2", "value2")
        );

        assertThatThrownBy(() -> globalVarsService.saveAllVars(varsWithWhitespaceKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ключ переменной не может быть null или пустым");

        verify(globalVarsRepository, never()).deleteAllInBatch();
        verify(globalVarsRepository, never()).saveAll(anyList());
    }

    /**
     * Проверяет, что при сохранении с null значением выбрасывается исключение.
     * Ожидается: IllegalArgumentException
     */
    @Test
    @DisplayName("Should throw exception when saving vars with null value")
    void shouldThrowExceptionWhenSavingVarsWithNullValue() {
        List<ReportGlobalVars> varsWithNullValue = Arrays.asList(
                createVar("KEY1", null),
                createVar("KEY2", "value2")
        );

        assertThatThrownBy(() -> globalVarsService.saveAllVars(varsWithNullValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Значение переменной не может быть null");

        verify(globalVarsRepository, never()).deleteAllInBatch();
        verify(globalVarsRepository, never()).saveAll(anyList());
    }

    /**
     * Проверяет, что при сохранении дубликатов с пробелами выбрасывается исключение.
     * Ожидается: IllegalArgumentException
     */
    @Test
    @DisplayName("Should throw exception when saving vars with duplicate keys with whitespace")
    void shouldThrowExceptionWhenSavingVarsWithDuplicateKeysWithWhitespace() {
        List<ReportGlobalVars> varsWithDuplicates = Arrays.asList(
                createVar("KEY1", "value1"),
                createVar("  KEY1  ", "value2"),
                createVar("KEY2", "value3")
        );

        assertThatThrownBy(() -> globalVarsService.saveAllVars(varsWithDuplicates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("В списке есть дубликаты ключей");

        verify(globalVarsRepository, never()).deleteAllInBatch();
        verify(globalVarsRepository, never()).saveAll(anyList());
    }

    /**
     * Проверяет, что при сохранении пустого списка все данные удаляются.
     * Ожидается: БД очищена, кэш обновлен
     */
    @Test
    @DisplayName("Should clear all vars when saving empty list")
    void shouldClearAllVarsWhenSavingEmptyList() {
        when(globalVarsRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(globalVarsRepository.findAll()).thenReturn(Collections.emptyList());

        List<ReportGlobalVars> result = globalVarsService.saveAllVars(Collections.emptyList());

        assertThat(result).isEmpty();
        verify(globalVarsRepository, times(1)).deleteAllInBatch();
        verify(globalVarsRepository, times(1)).saveAll(Collections.emptyList());
        verify(globalVarsRepository, times(1)).findAll();

        Map<String, String> cache = ReportGlobalVarsService.getGlobalVarsCache();
        assertThat(cache).isEmpty();
    }

    /**
     * Проверяет, что после сохранения кэш обновляется.
     * Ожидается: кэш содержит новые данные
     */
    @Test
    @DisplayName("Should refresh cache after saving")
    void shouldRefreshCacheAfterSaving() {
        List<ReportGlobalVars> newVars = Arrays.asList(
                createVar("NEW_KEY1", "new_value1"),
                createVar("NEW_KEY2", "new_value2")
        );

        when(globalVarsRepository.saveAll(anyList())).thenReturn(newVars);
        when(globalVarsRepository.findAll()).thenReturn(newVars);

        List<ReportGlobalVars> result = globalVarsService.saveAllVars(newVars);

        assertThat(result).hasSize(2);
        verify(globalVarsRepository, times(1)).findAll();

        Map<String, String> cache = ReportGlobalVarsService.getGlobalVarsCache();
        assertThat(cache)
                .hasSize(2)
                .containsEntry("NEW_KEY1", "new_value1")
                .containsEntry("NEW_KEY2", "new_value2");
    }

    /**
     * Проверяет, что getAllVars возвращает все переменные в правильном порядке.
     * Ожидается: список отсортирован по ключу
     */
    @Test
    @DisplayName("Should return all vars ordered by key")
    void shouldReturnAllVarsOrderedByKey() {
        when(globalVarsRepository.findAllByOrderByKeyAsc()).thenReturn(testVars);

        List<ReportGlobalVars> result = globalVarsService.getAllVars();

        assertThat(result)
                .hasSize(3)
                .extracting(ReportGlobalVars::getKey)
                .containsExactly("KEY1", "KEY2", "KEY3");

        verify(globalVarsRepository, times(1)).findAllByOrderByKeyAsc();
    }

    /**
     * Проверяет, что при сохранении с большим количеством переменных все сохраняется.
     * Ожидается: все переменные сохранены
     */
    @Test
    @DisplayName("Should save large list of vars")
    void shouldSaveLargeListOfVars() {
        List<ReportGlobalVars> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeList.add(createVar("KEY_" + i, "value_" + i));
        }

        when(globalVarsRepository.saveAll(anyList())).thenReturn(largeList);
        when(globalVarsRepository.findAll()).thenReturn(largeList);

        List<ReportGlobalVars> result = globalVarsService.saveAllVars(largeList);

        assertThat(result).hasSize(100);
        verify(globalVarsRepository, times(1)).deleteAllInBatch();
        verify(globalVarsRepository, times(1)).saveAll(largeList);
    }

    /**
     * Проверяет, что возвращается неизменяемая копия кэша.
     * Ожидается: попытка изменения кэша выбрасывает UnsupportedOperationException
     */
    @Test
    @DisplayName("Should return unmodifiable cache")
    void shouldReturnUnmodifiableCache() {
        when(globalVarsRepository.findAll()).thenReturn(testVars);
        globalVarsService.init();

        Map<String, String> cache = ReportGlobalVarsService.getGlobalVarsCache();

        assertThatThrownBy(() -> cache.put("NEW_KEY", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Проверяет, что при ошибке сохранения кэш не обновляется.
     * Ожидается: старый кэш сохраняется
     */
    @Test
    @DisplayName("Should not update cache when save fails")
    void shouldNotUpdateCacheWhenSaveFails() {
        when(globalVarsRepository.findAll()).thenReturn(testVars);
        globalVarsService.init();

        Map<String, String> oldCache = ReportGlobalVarsService.getGlobalVarsCache();
        assertThat(oldCache).hasSize(3);

        List<ReportGlobalVars> newVars = Arrays.asList(
                createVar("NEW_KEY", "new_value")
        );
        when(globalVarsRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> globalVarsService.saveAllVars(newVars))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        // Проверяем, что кэш не изменился
        Map<String, String> cache = ReportGlobalVarsService.getGlobalVarsCache();
        assertThat(cache).hasSize(3).containsEntry("KEY1", "value1");

        verify(globalVarsRepository, times(1)).deleteAllInBatch();
        verify(globalVarsRepository, times(1)).saveAll(anyList());
        verify(globalVarsRepository, times(1)).findAll();
    }
}