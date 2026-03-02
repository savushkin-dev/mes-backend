package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.postgres.ReportGlobalVars;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportGlobalVarsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReportGlobalVarsService {

    private final ReportGlobalVarsRepository globalVarsRepository;

    @Autowired
    public ReportGlobalVarsService(ReportGlobalVarsRepository globalVarsRepository) {
        this.globalVarsRepository = globalVarsRepository;
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

        return globalVarsRepository.saveAll(vars);
    }

}
