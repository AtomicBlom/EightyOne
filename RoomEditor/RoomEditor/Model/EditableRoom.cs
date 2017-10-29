using RoomEditor.Framework;

namespace RoomEditor.Model
{
    public class EditableRoom : BindableBase
    {
        private bool _present;

        public EditableRoom(int x, int z, bool present)
        {
            X = x;
            Z = z;
            Present = present;
        }

        public int X { get; }
        public int Z { get; }

        public bool Present
        {
            get => _present;
            set => SetField(ref _present, value);
        }
    }
}