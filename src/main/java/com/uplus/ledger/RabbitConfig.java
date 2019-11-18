package com.uplus.ledger;

import java.util.HashMap;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

@Configuration
public class RabbitConfig implements RabbitListenerConfigurer {

	@Autowired
	public Environment environment;
    
    @Bean
    Queue LedgerQueue() {    	
    	return QueueBuilder.nonDurable(environment.getProperty("ledger.queue")).autoDelete().build();
    }
    
    @Bean
    Queue LedgerQueuePrivate() {    	
    	HashMap < String, Object > arguments = new HashMap < String, Object >();
        return QueueBuilder.nonDurable(environment.getProperty("ledger.queue.private")).autoDelete().withArguments(arguments).build();
    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        return messageHandlerMethodFactory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }
}