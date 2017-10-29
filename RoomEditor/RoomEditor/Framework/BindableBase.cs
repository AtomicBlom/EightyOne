using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using RoomEditor.Annotations;

namespace RoomEditor.Framework
{
    public class BindableBase : INotifyPropertyChanged
    {
        [NotifyPropertyChangedInvocator]
        protected bool SetField<T>(ref T field, T value, [CallerMemberName] string propertyName = null)
        {
            if (EqualityComparer<T>.Default.Equals(field, value)) return false;
            field = value;
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
            return true;
        }

        protected void RaisePropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        public event PropertyChangedEventHandler PropertyChanged;
    }
}
