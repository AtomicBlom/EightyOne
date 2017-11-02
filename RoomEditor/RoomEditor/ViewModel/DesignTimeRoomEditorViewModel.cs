using System.Linq;
using RoomEditor.Model;

namespace RoomEditor.ViewModel
{
    public class DesignTimeRoomEditorViewModel : RoomEditorViewModel
    {
        public DesignTimeRoomEditorViewModel()
        {
            RoomProfiles.Add(new RoomSet
            {
                Name = "Room U",
                Rooms = new []
                    {
                        true, false, true,
                        true, false, true,
                        true, true, true
                    }.Select((value, index) => new Room(index % RoomSize, index / RoomSize, value))
                    .ToArray()
            });
            RoomProfiles.Add(new RoomSet
            {
                Name = "T",
                Rooms = new []
                    {
                        true, true, true,
                        false, true, false,
                        false, true, false
                    }.Select((value, index) => new Room(index % RoomSize, index / RoomSize, value))
                    .ToArray()
            });
            RoomProfiles.Add(new RoomSet
            {
                Name = "V",
                Rooms = new[]
                    {
                        true, false, true,
                        true, false, true,
                        false, true, false
                    }.Select((value, index) => new Room(index % RoomSize, index / RoomSize, value))
                    .ToArray()
            });

            this.CurrentRoomSet = RoomProfiles.First();
            this.TestRoomSet = RoomProfiles.Last();
        }
    }
}