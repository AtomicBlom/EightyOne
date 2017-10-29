using System.Linq;
using RoomEditor.Model;

namespace RoomEditor.ViewModel
{
    public class DesignTimeRoomEditorViewModel : RoomEditorViewModel
    {
        public DesignTimeRoomEditorViewModel()
        {
            RoomProfiles.Add(new RoomProfile
            {
                Name = "U",
                Rooms = new []
                    {
                        true, false, true,
                        true, false, true,
                        true, true, true
                    }.Select((value, index) => new EditableRoom(index % RoomSize, index / RoomSize, value))
                    .ToArray()
            });
            RoomProfiles.Add(new RoomProfile
            {
                Name = "T",
                Rooms = new []
                    {
                        true, true, true,
                        false, true, false,
                        false, true, false
                    }.Select((value, index) => new EditableRoom(index % RoomSize, index / RoomSize, value))
                    .ToArray()
            });

            this.CurrentRoomProfile = RoomProfiles.First();
            this.TestRoomProfile = RoomProfiles.Last();
        }
    }
}