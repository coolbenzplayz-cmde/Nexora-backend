package org.example.nexora.media;

import org.example.nexora.media.dto.CreateEditingJobRequest;
import org.example.nexora.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private EditingJobRepository editingJobRepository;
    @InjectMocks
    private MediaService mediaService;

    @Test
    void createJobPersists() {
        User user = new User();
        user.setId(2L);
        CreateEditingJobRequest req = new CreateEditingJobRequest();
        req.setJobType(EditingJob.EditingJobType.IMAGE);
        req.setSourceUri("https://cdn.example.com/in.png");

        when(editingJobRepository.save(any(EditingJob.class))).thenAnswer(invocation -> {
            EditingJob j = invocation.getArgument(0);
            j.setId(10L);
            return j;
        });

        EditingJob job = mediaService.createJob(user, req);
        assertNotNull(job);
        assertEquals(10L, job.getId());
        assertEquals(EditingJob.EditingJobStatus.QUEUED, job.getStatus());
    }
}
