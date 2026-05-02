package fr.akai;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class MPK249ExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("935cb4f5-33c7-473a-aef7-bc8b3abdb133");
   
   public MPK249ExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "MPK249";
   }
   
   @Override
   public String getAuthor()
   {
      return "sebj84";
   }

   @Override
   public String getVersion()
   {
      return "0.1";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }
   
   @Override
   public String getHardwareVendor()
   {
      return "Akai";
   }
   
   @Override
   public String getHardwareModel()
   {
      return "MPK249";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 25;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 2;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 2;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
      if (platformType == PlatformType.WINDOWS)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         list.add
         (
        		 new String[]{"MPK249", "MIDIIN4 (MPK249)"},
        		 //new String[]{"MIDIIN2 (MPK249)", "MIDIIN3 (MPK249)"},
        		 new String[]{"MPK249", "MIDIOUT4 (MPK249)"}
        		 //new String[]{"MIDIOUT2 (MPK249)", "MIDIOUT3 (MPK249)"}
         );
      }
      else if (platformType == PlatformType.MAC)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Input Port 0", "Input Port 1", "Input Port 2", "Input Port 3"}, new String[]{"Output Port 0", "Output Port 1", "Output Port 2", "Output Port 3"});
      }
      else if (platformType == PlatformType.LINUX)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Input Port 0", "Input Port 1", "Input Port 2", "Input Port 3"}, new String[]{"Output Port 0", "Output Port 1", "Output Port 2", "Output Port 3"});
      }
   }

   @Override
   public MPK249Extension createInstance(final ControllerHost host)
   {
      return new MPK249Extension(this, host);
   }
}
