package me.firesmasher.flarecord.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import java.util.List;
import me.firesmasher.flarecord.Flarecord;
import me.firesmasher.flarecord.runtime.FlarecordRuntime;

public final class FlarecordCommand implements SimpleCommand {

  private final FlarecordRuntime runtime;

  public FlarecordCommand(FlarecordRuntime runtime) {
    this.runtime = runtime;
  }

  @Override
  public void execute(final Invocation invocation) {
    final CommandSource source = invocation.source();
    source.sendPlainMessage("Flarecord status");
    source.sendPlainMessage("Name: " + Flarecord.getName());
    source.sendPlainMessage("Version: " + Flarecord.getVersion());
    source.sendPlainMessage("Vendor: " + Flarecord.getVendor());
    source.sendPlainMessage("Features: "
        + (runtime.getConfiguration().isEnabled() ? "enabled" : "disabled"));
    source.sendPlainMessage("Queue: " + (runtime.getConfiguration().isQueueEnabled()
        ? runtime.getPlayerQueue().size() + "/" + runtime.getPlayerQueue().capacity()
        : "disabled"));
    source.sendPlainMessage("Anti-bot: "
        + (runtime.getConnectionGuard().isEnabled() ? "enabled" : "disabled")
        + " (" + runtime.getConnectionGuard().getRejectedConnectionCount()
        + " rejected connections)");
    source.sendPlainMessage("Animations: " + runtime.getAnimations().getAnimations().size()
        + " loaded");
  }

  @Override
  public List<String> suggest(final Invocation invocation) {
    return List.of("status", "version", "config");
  }
}
