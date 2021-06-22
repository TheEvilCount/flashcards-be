package cz.cvut.fel.poustka.daniel.flashcards_backend.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;

@Component
public class SystemInitializer
{

    private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

    private final PlatformTransactionManager txManager;

    @Autowired
    public SystemInitializer(PlatformTransactionManager txManager)
    {
        this.txManager = txManager;
    }

    @PostConstruct
    private void initSystem()
    {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
    }
}