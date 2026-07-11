package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.postgres.ReportGlobalVars;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportGlobalVarsRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

        Set<String> keys = new HashSet<>();
        vars.forEach(var -> {
            String key = var.getKey();
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Ключ переменной не может быть null или пустым");
            }
            if (var.getValue() == null) {
                throw new IllegalArgumentException("Значение переменной не может быть null");
            }
            if (!keys.add(key.trim())) {
                throw new IllegalArgumentException("В списке есть дубликаты ключей");
            }
        });

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
