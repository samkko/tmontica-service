package com.internship.tmontica.menu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MenuScheduler {
    private final MenuDao menuDao;
    private static List<Menu> usableMenus;

    public MenuScheduler(MenuDao menuDao){
        this.menuDao = menuDao;
        usableMenus = new ArrayList<>();
    }

    @Scheduled(cron = "0 * * * * *")
    public void filteredMenu(){
        log.info("[scheduler] start scheduler");
        List<Menu> allMenus = menuDao.getAllMenus();
        List<Menu> filteredMenus = new ArrayList<>();
        Date now = new Date();

        Predicate<Menu> isDeleted= Menu::isDeleted;
        Predicate<Menu> con1 = menu -> menu.getStartDate() == null && menu.getEndDate() == null;
        Predicate<Menu> con2 = menu -> menu.getStartDate().before(now) && menu.getEndDate().after(now);

        filteredMenus = allMenus.stream().filter(Menu::isUsable).filter(isDeleted.negate())
                .filter(con1.or(con2)).collect(Collectors.toList());

        usableMenus = filteredMenus;
        log.info("[scheduler] end scheduler , usableMenus size : {}", usableMenus.size());
    }

    public static List<Menu> getUsableMenus(){
        return usableMenus;
    }

}
