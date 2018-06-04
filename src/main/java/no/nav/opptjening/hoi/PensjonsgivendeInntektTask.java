package no.nav.opptjening.hoi;

import no.nav.opptjening.skatt.exceptions.HttpException;
import no.nav.opptjening.skatt.schema.BeregnetSkatt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class PensjonsgivendeInntektTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PensjonsgivendeInntektTask.class);

    private final SkatteoppgjorhendelseConsumer hendelseConsumer;
    private final PensjonsgivendeInntektKafkaProducer inntektProducer;

    public PensjonsgivendeInntektTask(SkatteoppgjorhendelseConsumer hendelseConsumer, PensjonsgivendeInntektKafkaProducer inntektProducer) {
        this.hendelseConsumer = hendelseConsumer;
        this.inntektProducer = inntektProducer;
    }

    public boolean currentThreadIsInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    public void run() {
        try {
            while (!currentThreadIsInterrupted()) {
                List<BeregnetSkatt> beregnetSkattList = hendelseConsumer.poll();
                inntektProducer.send(beregnetSkattList);
                hendelseConsumer.commit();
            }
        } catch (HttpException e) {
            LOG.error("Error while contacting Skatteetaten", e);
        } catch (IOException e) {
            LOG.error("Error during hendelseConsumer.poll()", e);
        } catch (Exception e) {
            LOG.error("Error during processing of Hendelse/Inntekt", e);
        }
        LOG.info("PensjonsgivendeInntektTask task stopped");
    }
}
