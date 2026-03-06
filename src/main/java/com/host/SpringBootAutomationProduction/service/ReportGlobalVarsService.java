package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.postgres.ReportGlobalVars;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportGlobalVarsRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReportGlobalVarsService {

    private final ReportGlobalVarsRepository globalVarsRepository;

    // Статический кэш для доступа из ReportUtil
    private static Map<String, String> varsCache = new ConcurrentHashMap<>();


    @Autowired
    public ReportGlobalVarsService(ReportGlobalVarsRepository globalVarsRepository) {
        this.globalVarsRepository = globalVarsRepository;
    }

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public List<ReportGlobalVars> getAllVars() {
        log.debug("Получение всех глобальных переменных");
        return globalVarsRepository.findAllByOrderByKeyAsc();
    }

    @Transactional
    public List<ReportGlobalVars> saveAllVars(List<ReportGlobalVars> vars) {
        log.debug("Сохранение всего списка переменных. Получено: {}, текущих: {}",
                vars.size(), globalVarsRepository.count());

        // Проверяем уникальность ключей в присланном списке
        long uniqueKeysCount = vars.stream()
                .map(ReportGlobalVars::getKey)
                .distinct()
                .count();

        if (uniqueKeysCount != vars.size()) {
            throw new IllegalArgumentException("В списке есть дубликаты ключей");
        }

        globalVarsRepository.deleteAllInBatch();
        List<ReportGlobalVars> saved = globalVarsRepository.saveAll(vars);

        // Обновляем кэш после сохранения
        refreshCache();

        return saved;
    }

    public static Map<String, String> getGlobalVarsCache() {
        // Возвращаем неизменяемое представление
        return Collections.unmodifiableMap(varsCache);
    }

    private void refreshCache() {
        List<ReportGlobalVars> vars = globalVarsRepository.findAll();
        varsCache = vars.stream()
                .collect(Collectors.toConcurrentMap(
                        ReportGlobalVars::getKey,
                        ReportGlobalVars::getValue,
                        (v1, v2) -> v1
                ));
        log.debug("Кэш глобальных переменных отчета обновлен. Загружено: {} переменных", varsCache.size());
    }

}
