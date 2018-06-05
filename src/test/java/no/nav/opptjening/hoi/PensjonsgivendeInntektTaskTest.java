package no.nav.opptjening.hoi;

import no.nav.opptjening.skatt.schema.BeregnetSkatt;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import no.nav.opptjening.skatt.exceptions.HttpException;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class PensjonsgivendeInntektTaskTest {

    @InjectMocks
    private PensjonsgivendeInntektTask pensjonsgivendeInntektTask;

    @Mock
    private SkatteoppgjorhendelseConsumer hendelseConsumer;

    @Mock
    private PensjonsgivendeInntektKafkaProducer inntektProducer;

    @Mock
    private HttpException httpException;

    private Thread testRunThread;

    @Before
    public void setUp() {
        testRunThread = new Thread(pensjonsgivendeInntektTask);
    }

    @Test
    public void runOk() throws Exception {
        Answer answer = invocationOnMock -> {
            System.out.println("Hei");
            testRunThread.interrupt();
            return null;
        };

        when(hendelseConsumer.poll()).thenReturn(Collections.emptyList()).thenAnswer(answer);

        testRunThread.start();

        verify(hendelseConsumer, times(2)).poll();
        verify(inntektProducer, times(2)).send(anyList());
        verify(hendelseConsumer, times(2)).commit();

        testRunThread.join(1000);
    }


    @Test
    public void runThrowsIOExceptionWhenSendIsCalled() throws Exception {
        doThrow(new IOException()).when(hendelseConsumer).poll();
        testRunThread.run();
        verify(hendelseConsumer).poll();
        verify(inntektProducer, times(0)).send(anyList());
        verify(hendelseConsumer, times(0)).commit();
    }

    @Test
    public void runThrowsHttpExceptionWhenPollIsCalled() throws Exception {
        doThrow(httpException).when(hendelseConsumer).poll();
        testRunThread.run();
        verify(hendelseConsumer).poll();
        verify(inntektProducer, times(0)).send(anyList());
        verify(hendelseConsumer, times(0)).commit();
    }

}
