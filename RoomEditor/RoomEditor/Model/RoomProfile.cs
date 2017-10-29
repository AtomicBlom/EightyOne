using RoomEditor.Framework;

namespace RoomEditor.Model
{
    public class RoomProfile : BindableBase
    {
        private string _name;

        public string Name
        {
            get => _name;
            set => SetField(ref _name, value);
        }

        public EditableRoom[] Rooms { get; set; }
        public int Id { get; set; }
    }
}