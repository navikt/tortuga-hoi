package no.nav.opptjening.hoi;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import java.io.IOException;

import no.nav.opptjening.skatt.exceptions.HttpException;

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

    @Before
    public void setUp() {
        pensjonsgivendeInntektTask = Mockito.spy(pensjonsgivendeInntektTask);
    }


    @Test
    public void runOk() throws Exception {
        when(pensjonsgivendeInntektTask.currentThreadIsInterrupted())
                .thenReturn(false)
                .thenReturn(true);

        pensjonsgivendeInntektTask.run();

        verify(hendelseConsumer).poll();
        verify(inntektProducer).send(anyList());
        verify(hendelseConsumer).commit();
    }

    @Test
    public void runThrowsIOExceptionWhenSendIsCalled() throws Exception {
        doThrow(IOException.class).when(hendelseConsumer).poll();

        when(pensjonsgivendeInntektTask.currentThreadIsInterrupted())
                .thenReturn(false)
                .thenReturn(true);

        pensjonsgivendeInntektTask.run();

        verify(hendelseConsumer).poll();
        verify(inntektProducer, times(0)).send(anyList());
        verify(hendelseConsumer, times(0)).commit();
    }

    @Test
    public void runThrowsHttpExceptionWhenPollIsCalled() throws Exception {
        doThrow(httpException).when(hendelseConsumer).poll();

        when(pensjonsgivendeInntektTask.currentThreadIsInterrupted())
                .thenReturn(false)
                .thenReturn(true);

        pensjonsgivendeInntektTask.run();

        verify(hendelseConsumer).poll();
        verify(inntektProducer, times(0)).send(anyList());
        verify(hendelseConsumer, times(0)).commit();
    }
}
