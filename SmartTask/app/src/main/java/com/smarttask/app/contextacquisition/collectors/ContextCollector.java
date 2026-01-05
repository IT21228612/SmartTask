package com.smarttask.app.contextacquisition.collectors;

import com.smarttask.app.contextacquisition.db.ContextSnapshot;

public interface ContextCollector {
    void collect(ContextSnapshot snapshot, CollectorContext context);
}
